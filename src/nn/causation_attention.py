# For conditions of distribution and use, see copyright notice in CausationLearning.java

# Causation attention network.
# results written to causation_rnn_results.txt
# Refs:
# https://www.jeremyjordan.me/attention/
# https://github.com/philipperemy/keras-attention

import os
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"
import numpy as np
from numpy import array, argmax
from keras import Model
from keras.layers import Layer
from attention import Attention
import keras.backend as K
from keras.layers import Input, Dense, SimpleRNN, LSTM, TimeDistributed
from sklearn.preprocessing import MinMaxScaler
from keras.models import Sequential
from keras.metrics import mean_squared_error
import sys, getopt

# define RNN configuration
n_hidden = 128
n_attention = 64
n_epochs = 500

# results file name
results_filename = 'causation_attention_results.json'

# prediction significance threshold
threshold = 0.5

# verbosity
verbose = True

# get options
usage = 'causation_attention.py [-h <hidden neurons>] [-a <attention neurons>] [-e <epochs>] [-q (quiet)]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"?qh:a:e:",["hidden=","attention=","epochs="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt in ("-?", "--help"):
     print(usage)
     sys.exit(0)
  if opt in ("-h", "--hidden"):
     n_hidden = int(arg)
  elif opt in ("-a", "--attention"):
     n_attention = int(arg)
  elif opt in ("-e", "--epochs"):
     n_epochs = int(arg)
  elif opt == "-q":
     verbose = False
  else:
     print(usage)
     sys.exit(1)

# import dataset
if verbose:
    print('Importing dataset from causation_attention_dataset.py')
from causation_attention_dataset import X_train_shape, X_train_seq, y_train_shape, y_train_seq
if X_train_shape[0] == 0:
    print('Empty train dataset')
    sys.exit(1)
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1])
num_samples, time_steps, input_dim, output_dim = X_train_shape[0], X_train_shape[1], X_train_shape[2], y_train_shape[1]
x_response = output_dim - 1

# create model
model_input = Input(shape=(time_steps, input_dim))
x = LSTM(n_hidden, return_sequences=True)(model_input)
x = Attention(units=n_attention)(x)
x = Dense(output_dim)(x)
model = Model(model_input, x)
model.compile(loss='mae', optimizer='adam')
if verbose:
    model.summary()

# train
model.fit(X, y, epochs=n_epochs, batch_size=num_samples, verbose=int(verbose))

# validate training
predictions = model.predict(X, batch_size=num_samples, verbose=int(verbose))
trainOK = 0
trainTotal = num_samples
if verbose:
    print('Train:')
for response in range(trainTotal):
    a = predictions[response]
    p = [i for i,v in enumerate(a) if v > threshold]
    a = y[response]
    t = [i for i,v in enumerate(a) if v > threshold]
    if p == t:
        trainOK += 1
    if verbose:
        t=['X' if x==x_response else x for x in t]
        p=['X' if x==x_response else x for x in p]
        print('target: {', ' '.join(map(str, t)), '} predicted: {', ' '.join(map(str, p)), '}', end='')
        if p == t:
            print(' OK')
        else:
            print(' error')

# predict
from causation_attention_dataset import X_test_shape, X_test_seq, y_test_shape, y_test_seq
num_samples = X_test_shape[0]
testOK = 0
testTotal = num_samples
if testTotal > 0:
    seq = array(X_test_seq)
    X = seq.reshape(X_test_shape[0], X_test_shape[1], X_test_shape[2])
    seq = array(y_test_seq)
    y = seq.reshape(y_test_shape[0], y_test_shape[1])
    predictions = model.predict(X, batch_size=num_samples, verbose=0)
    if verbose:
        print('Test:')
    for response in range(testTotal):
        a = predictions[response]
        p = [i for i,v in enumerate(a) if v > threshold]
        a = y[response]
        t = [i for i,v in enumerate(a) if v > threshold]
        if p == t:
            testOK += 1
        if verbose:
            t=['X' if x==x_response else x for x in t]
            p=['X' if x==x_response else x for x in p]
            print('target: {', ' '.join(map(str, t)), '} predicted: {', ' '.join(map(str, p)), '}', end='')
            if p == t:
                print(' OK')
            else:
                print(' error')

trainPct=0
if trainTotal > 0:
    trainPct = (float(trainOK) / float(trainTotal)) * 100.0
testPct=0
if testTotal > 0:
    testPct = (float(testOK) / float(testTotal)) * 100.0

# print results
if verbose == True:
    print("Train correct/total = ", trainOK, "/", trainTotal, sep='', end='')
    print(" (", str(round(trainPct, 2)), "%)", sep='')
    print("Test correct/total = ", testOK, "/", testTotal, sep='', end='')
    print(" (", str(round(testPct, 2)), "%)", sep='')

# write results
if verbose:
    print('Writing results to causation_attention_results.json')
with open(results_filename, 'w') as f:
    f.write('{')
    f.write('\"train_correct_predictions\":\"'+str(trainOK)+'\",')
    f.write('\"train_total_predictions\":\"'+str(trainTotal)+'\",')
    f.write('\"train_pct\":\"'+str(round(trainPct, 2))+'\",')
    f.write('\"test_correct_predictions\":\"'+str(testOK)+'\",')
    f.write('\"test_total_predictions\":\"'+str(testTotal)+'\",')
    f.write('\"test_pct\":\"'+str(round(testPct, 2))+'\"')
    f.write('}\n')
    
sys.exit(0)
