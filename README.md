# Causation learning.

Learn causations in event streams that contain cause-and-effect sequences. A cause-and-effect
sequence has a single effect event and one or more cause events, occurring in an arbitrary order. 
Cause and effect events can be distributed over time, meaning they can 
be separated by 0 or more intervening events.

Several learning methods are available:<br>
LSTM: Long Short-Term Memory neural network.<br>
Simple RNN: Simple recurrent neural network.<br>
Attention: LSTM with attention layer.<br>
NN: Multilayer perceptron (non-recurrent).<br>
GA: Genetic algorithm.<br>

One aim of this project is to build more generalized mediator neurons for the Mona neural network.

Develop: Import Eclipse project.

Build:
<pre>
build.sh or build.bat
</pre>

Run:
<pre>
causation.sh or causation.bat
</pre>

Usage:
<pre>
  Run:
    java mona.causation.CausationLearning
        [-numEventTypes <quantity> (default=10)]
        [-numCauseEventTypes <quantity> (default=5)]
        [-numCausations <quantity> (default=2)]
        [-maxCauseEvents <quantity> (default=2)]
        [-eventOmissionProbability <probability> (default=0.1)]
        [-maxInterveningEvents <quantity> (default=2)]
        [-maxValidInterveningEvents <quantity> (default=1)]
        [-validInterveningEventsProbability <probability> (default=0.9)]
        [-numTrainingCausationInstances <quantity> (default=10)]
        [-numTestingCausationInstances <quantity> (default=10)]
        [-learner
           "LSTM" | "SimpleRNN" | "Attention" | "NN" |
             [-numHiddenNeurons <quantity> (default=128) (repeat for additional layers)]
             [-numEpochs <quantity> (default=500)]
           "GA" (default=LSTM)]
             [-generations <quantity> (default=10)]
             [-populationSize <quantity> (default=20)]
             [-fitPopulationSize <quantity> (default=10)]
             [-mutationRate <probability> (default=0.25)]
        [-randomSeed <random number seed> (default=4517)]
        [-verbose (default=false)]        
  Print parameters:
    java mona.causation.CausationLearning -printParameters
  Version:
    java mona.causation.CausationLearning -version
Exit codes:
  0=success
  1=error
</pre>
