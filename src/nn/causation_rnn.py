# For conditions of distribution and use, see copyright notice in Main.java

# Causation RNN.
# results written to causation_rnn_results.txt
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"
from numpy import array, argmax
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import TimeDistributed
from keras.layers import SimpleRNN, LSTM
import sys, getopt

# define RNN configuration
network = 'LSTM'
n_hidden = [128]
n_epochs = 500

# results file name
results_filename = 'causation_rnn_results.json'

# verbosity
verbose = True

# get options
first_hidden = True
usage = 'causation_rnn.py [-n LSTM | SimpleRNN ] [-h <hidden neurons> (repeat for additional layers)] [-e <epochs>] [-q (quiet)]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"?qn:h:e:",["network=", "hidden=","epochs="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt in ("-?", "--help"):
     print(usage)
     sys.exit(0)
  if opt in ("-n", "--network"):
     network = arg
     if network != 'LSTM' and network != 'SimpleRNN':
         print('Invalid network type')
         print(usage)
         sys.exit(1)
  elif opt in ("-h", "--hidden"):
     if first_hidden:
         first_hidden = False
         n_hidden = []
     n_hidden.append(int(arg))
  elif opt in ("-e", "--epochs"):
     n_epochs = int(arg)
  elif opt == "-q":
     verbose = False
  else:
     print(usage)
     sys.exit(1)

# prepare data
from causation_rnn_dataset import X_train_shape, X_train_seq, y_train_shape, y_train_seq
if X_train_shape[0] == 0:
    print('Empty train dataset')
    sys.exit(1)
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1], y_train_shape[2])

# create RNN
model = Sequential()
if network == 'LSTM':
    model.add(LSTM(n_hidden[0], input_shape=(X_train_shape[1], X_train_shape[2]), return_sequences=True))
    for i in range(1, len(n_hidden)):
        model.add(LSTM(n_hidden[i], return_sequences=True))
else:
    model.add(SimpleRNN(n_hidden[0], input_shape=(X_train_shape[1], X_train_shape[2]), return_sequences=True))
    for i in range(1, len(n_hidden)):
        model.add(SimpleRNN(n_hidden[i], return_sequences=True))
model.add(TimeDistributed(Dense(y_train_shape[2])))
model.compile(loss='mean_squared_error', optimizer='adam')
if verbose:
    model.summary()

# train
model.fit(X, y, epochs=n_epochs, batch_size=X_train_shape[0], verbose=int(verbose))

# validate training
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1], y_train_shape[2])
predictions = model.predict(X, batch_size=X_train_shape[0], verbose=int(verbose))
trainOK = 0
trainTotal = X_train_shape[0]
trainSteps = X_train_shape[1]
if verbose:
    print('Train:')
for path in range(trainTotal):
    p = []
    for step in range(trainSteps):
        if X[path][step][-1] == 1:
            r = argmax(predictions[path][step])
            p.append(r)
    t = []
    for step in range(trainSteps):
        if X[path][step][-1] == 1:
            r = argmax(y[path][step])
            t.append(r)
    if p[0] == t[0]:
        trainOK += 1
    if verbose:
        print('target:', t[0], 'predicted:', p[0], end='')
        if p[0] == t[0]:
            print(' OK')
        else:
            print(' error')

# predict
from causation_rnn_dataset import X_test_shape, X_test_seq, y_test_shape, y_test_seq
if X_test_shape[0] == 0:
    print('Empty test dataset')
    sys.exit(1)
seq = array(X_test_seq)
X = seq.reshape(X_test_shape[0], X_test_shape[1], X_test_shape[2])
seq = array(y_test_seq)
y = seq.reshape(y_test_shape[0], y_test_shape[1], y_test_shape[2])
testOK = 0
testTotal = X_test_shape[0]
testSteps = X_test_shape[1]
if testTotal > 0:
    predictions = model.predict(X, batch_size=X_test_shape[0], verbose=0)
    if verbose:
        print('Test:')
    for path in range(testTotal):
        p = []
        for step in range(testSteps):
            if X[path][step][-1] == 1:
                r = argmax(predictions[path][step])
                p.append(r)
        t = []
        for step in range(testSteps):
            if X[path][step][-1] == 1:
                r = argmax(y[path][step])
                t.append(r)
        if p[0] == t[0]:
            testOK += 1
        if verbose:
            print('target:', t[0], 'predicted:', p[0], end='')
            if p[0] == t[0]:
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

# write results to causaation_rnn_results.txt
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


