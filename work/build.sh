#!/bin/bash
javac -classpath "../lib/*" -d . ../src/mona/causation/*.java
cp ../src/nn/causation_nn.py .
cp ../src/nn/causation_rnn.py .
cp ../src/nn/causation_attention.py .
cp ../src/nn/attention.py .
jar cvfm ../bin/causation.jar causation.mf mona causation_nn.py causation_rnn.py causation_attention.py attention.py
