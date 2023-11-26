#!/bin/bash
javac -classpath "../lib/*" -d . ../src/mona/causation/*.java
cp ../src/nn/causation_nn.py .
cp ../src/nn/causation_lstm.py .
cp ../src/nn/causation_attention.py .
jar cvfm ../bin/causation.jar causation.mf mona causation_nn.py causation_lstm.py causation_attention.py
