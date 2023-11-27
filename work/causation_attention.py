# Causation attention network.
# results written to causation_rnn_results.txt
# Refs:
# https://www.jeremyjordan.me/attention/
# https://machinelearningmastery.com/adding-a-custom-attention-layer-to-recurrent-neural-network-in-keras/
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"
import numpy as np
from numpy import array, argmax
from keras import Model
from keras.layers import Layer
import keras.backend as K
from keras.layers import Input, Dense, SimpleRNN, LSTM, TimeDistributed
from sklearn.preprocessing import MinMaxScaler
from keras.models import Sequential
from keras.metrics import mean_squared_error
import sys, getopt

# define RNN configuration
network = 'LSTM'
n_hidden = 128
n_epochs = 500

# results file name
results_filename = 'causation_rnn_results.json'

# verbosity
verbose = True

# get options
first_hidden = True
usage = 'causation_attention.py [-n LSTM | SimpleRNN ] [-h <hidden neurons>] [-e <epochs>] [-q (quiet)]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"?qn:h:e:",["network=", "hidden=","epochs="])
except getopt.GetoptError:
  print(usage)
  sys.exit(2)
for opt, arg in opts:
  if opt in ("-?", "--help"):
     print(usage)
     sys.exit(0)
  if opt in ("-r", "--network"):
     network = arg
     if network != 'LSTM' and network != 'SimpleRNN':
         print('Invalid network type')
         print(usage)
         sys.exit(1)
  elif opt in ("-h", "--hidden"):
     if first_hidden:
         first_hidden = False
         n_hidden = int(arg)
     else:
         print('invalid multiple hidden option')
         sys.exit(1)
  elif opt in ("-e", "--epochs"):
     n_epochs = int(arg)
  elif opt == "-q":
     verbose = False

# prepare data
from causation_rnn_dataset import X_train_shape, X_train_seq, y_train_shape, y_train_seq
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1], y_train_shape[2])

# Add attention layer to the network
class attention_layer(Layer):
    def __init__(self,**kwargs):
        super(attention_layer,self).__init__(**kwargs)

    def build(self,input_shape):
        self.W=self.add_weight(name='attention_weight', shape=(input_shape[-1],1),
                               initializer='random_normal', trainable=True)
        self.b=self.add_weight(name='attention_bias', shape=(input_shape[-2],1),
                               initializer='zeros', trainable=True)
        super(attention_layer, self).build(input_shape)

    def call(self,x):
        # Alignment scores. Pass them through tanh function
        e = K.tanh(K.dot(x,self.W)+self.b)
        # Remove dimension of size 1
        e = K.squeeze(e, axis=-1)
        # Compute the weights
        alpha = K.softmax(e)
        # Reshape to tensorFlow format
        alpha = K.expand_dims(alpha, axis=-1)
        # Compute the context vector
        context = x * alpha
        context = K.sum(context, axis=1)
        return context

def create_RNN_with_attention(network, input_shape, hidden_units, output_units, activation):
    x=Input(shape=input_shape)
    if network == 'SimpleRNN':
        RNN_layer = SimpleRNN(hidden_units, return_sequences=True, activation=activation)(x)
    else:
        RNN_layer = LSTM(hidden_units, input_shape=input_shape, return_sequences=True)(x)
    attention = attention_layer()(RNN_layer)
    outputs=Dense(output_units, trainable=True, activation=activation)(attention)
    model=Model(x,outputs)
    model.compile(loss='mse', optimizer='adam')    
    return model    

# Create the model
model = create_RNN_with_attention(network=network, input_shape=(X_train_shape[1], X_train_shape[2]),
                                   hidden_units=n_hidden, output_units=y_train_shape[2], activation='tanh')
if verbose:
    model.summary()

# Train
model.fit(X, y, epochs=n_epochs, batch_size=1, verbose=0)

# Evalute model
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1], y_train_shape[2])
predictions = model.predict(X, batch_size=X_train_shape[0], verbose=0)
trainOK = 0
trainErrors = 0
trainTotal = 0
for path in range(X_train_shape[0]):
    p = []
    for step in range(X_train_shape[1]):
        if not all([ v == 0 for v in y[path][step]]):
            if y[path][step][-1] == 0:
                r = argmax(predictions[path][step])
                p.append(r)
    t = []
    for step in range(X_train_shape[1]):
        if not all([ v == 0 for v in y[path][step]]):
            if y[path][step][-1] == 0:
                r = argmax(y[path][step])
                t.append(r)
    if p == t:
        trainOK += 1
    else:
        errs = 0
        for i in range(len(p)):
            if p[i] != t[i]:
                errs += 1
        trainErrors += errs
    trainTotal += len(p)

# predict
from causation_rnn_dataset import X_test_shape, X_test_seq, y_test_shape, y_test_seq
seq = array(X_test_seq)
X = seq.reshape(X_test_shape[0], X_test_shape[1], X_test_shape[2])
seq = array(y_test_seq)
y = seq.reshape(y_test_shape[0], y_test_shape[1], y_test_shape[2])
testOK = 0
testErrors = 0
testTotal = 0
if X_test_shape[0] > 0:
    predictions = model.predict(X, batch_size=X_test_shape[0], verbose=0)
    for path in range(X_test_shape[0]):
        p = []
        for step in range(X_test_shape[1]):
            if not all([ v == 0 for v in y[path][step]]):
                if y[path][step][-1] == 0:
                    r = argmax(predictions[path][step])
                    p.append(r)
        t = []
        for step in range(X_test_shape[1]):
            if not all([ v == 0 for v in y[path][step]]):
                if y[path][step][-1] == 0:
                    r = argmax(y[path][step])
                    t.append(r)
        if p == t:
            testOK += 1
        else:
            errs = 0
            for i in range(len(p)):
                if p[i] != t[i]:
                    errs += 1
            testErrors += errs
        testTotal += len(p)
trainErrorPct=0
if trainTotal > 0:
    trainErrorPct = (float(trainErrors) / float(trainTotal)) * 100.0
testErrorPct=0
if testTotal > 0:
    testErrorPct = (float(testErrors) / float(testTotal)) * 100.0

# print results
if verbose == True:
    print("Train correct paths/total = ", trainOK, "/", X_train_shape[0], sep='', end='')
    if X_train_shape[0] > 0:
        r = (float(trainOK) / float(X_train_shape[0])) * 100.0
        print(" (", str(round(r, 2)), "%)", sep='', end='')
    print(", prediction errors/total = ", trainErrors, "/", trainTotal, sep='', end='')
    if trainTotal > 0:
        print(" (", str(round(trainErrorPct, 2)), "%)", sep='', end='')
    print('')
    print("Test correct paths/total = ", testOK, "/", X_test_shape[0], sep='', end='')
    if X_test_shape[0] > 0:
        r = (float(testOK) / float(X_test_shape[0])) * 100.0
        print(" (", str(round(r, 2)), "%)", sep='', end='')
    print(", prediction errors/total = ", testErrors, "/", testTotal, sep='', end='')
    if testTotal > 0:
        print(" (", str(round(testErrorPct, 2)), "%)", sep='', end='')
    print('')

# Write results to causaation_rnn_results.txt
with open(results_filename, 'w') as f:
    f.write('{')
    f.write('\"train_prediction_errors\":\"'+str(trainErrors)+'\",')
    f.write('\"train_total_predictions\":\"'+str(trainTotal)+'\",')
    f.write('\"train_error_pct\":\"'+str(round(trainErrorPct, 2))+'\",')
    f.write('\"test_prediction_errors\":\"'+str(testErrors)+'\",')
    f.write('\"test_total_predictions\":\"'+str(testTotal)+'\",')
    f.write('\"test_error_pct\":\"'+str(round(testErrorPct, 2))+'\"')
    f.write('}\n')

sys.exit(0)
