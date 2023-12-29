# For conditions of distribution and use, see copyright notice in CausationLearning.java

# Causation NN.
# results written to causation_nn_results.txt
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"
from numpy import array, argmax
from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense
import sys, getopt

# parameters
n_hidden = [128]
n_epochs = 500

# results file name
results_filename = 'causation_nn_results.json'

# verbosity
verbose = True

# get options
first_hidden = True
usage = 'causation_nn.py [-h <hidden neurons> (repeat for additional layers)] [-e <epochs>] [-q (quiet)]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"?qh:e:",["hidden=","epochs="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt in ("-?", "--help"):
     print(usage)
     sys.exit(0)
  if opt in ("-h", "--hidden"):
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

# import dataset
if verbose:
    print('Importing dataset from causation_nn_dataset.py')
from causation_nn_dataset import X_train_shape, y_train_shape, X_train_seq, y_train_seq, X_test_shape, y_test_shape, X_test_seq, y_test_seq
if X_train_shape[0] == 0:
    print('Empty train dataset')
    sys.exit(1)
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1])

# create NN
model = Sequential()
model.add(Dense(n_hidden[0], input_dim=X_train_shape[1], activation='relu'))
for i in range(1, len(n_hidden)):
    model.add(Dense(n_hidden[i], activation='relu'))
model.add(Dense(y_train_shape[1], activation='sigmoid'))
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
if verbose:
    model.summary()

# train
model.fit(X, y, epochs=n_epochs, batch_size=X_train_shape[0], verbose=int(verbose))

# validate training
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1])
seq = array(y_train_seq)
y = seq.reshape(y_train_shape[0], y_train_shape[1])
predictions = model.predict(X, batch_size=X_train_shape[0], verbose=int(verbose))
trainOK = 0
trainTotal = X_train_shape[0]
if verbose:
    print('Train:')
for response in range(trainTotal):
    p = argmax(predictions[response])
    t = argmax(y[response])
    if p == t:
        trainOK += 1
        if verbose:
            print('target:', t, 'predicted:', p, end='')
            if p == t:
                print(' OK')
            else:
                print(' error')

# predict
testOK = 0
testTotal = X_test_shape[0]
if testTotal > 0:
    seq = array(X_test_seq)
    X = seq.reshape(X_test_shape[0], X_test_shape[1])
    seq = array(y_test_seq)
    y = seq.reshape(y_test_shape[0], y_test_shape[1])
    predictions = model.predict(X, batch_size=testTotal, verbose=0)
    if verbose:
        print('Test:')
    for response in range(testTotal):
        p = argmax(predictions[response])
        t = argmax(y[response])
        if p == t:
            testOK += 1
        if verbose:
            print('target:', t, 'predicted:', p, end='')
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
    print('Writing results to causation_nn_results.json')
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
