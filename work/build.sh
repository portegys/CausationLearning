#!/bin/bash
javac -d . ../src/mona/causation/*.java
jar cvfm ../bin/causation.jar causation.mf mona
