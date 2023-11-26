javac -d . ../src/mona/causation/*.java
copy ..\src\nn\causation_nn.py .
copy ..\src\nn\causation_lstm.py .
copy ..\src\nn\causation_attention.py .
jar cvfm ../bin/causation.jar causation.mf mona causation_nn.py causation_lstm.py causation_attention.py