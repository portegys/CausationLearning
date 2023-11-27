javac -d . ../src/mona/causation/*.java
copy ..\src\nn\causation_nn.py .
copy ..\src\nn\causation_rnn.py .
copy ..\src\nn\causation_attention.py .
jar cvfm ../bin/causation.jar causation.mf mona causation_nn.py causation_rnn.py causation_attention.py