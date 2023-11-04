javac -d . ../src/mona/causation/*.java
copy ..\src\nn\causation_lstm.py .
jar cvfm ../bin/causation.jar causation.mf mona causation_lstm.py