# Causation learning.

Learn causations in event streams that contain cause-and-effect sequences. A cause-and-effect
sequence has a single effect event and one or more cause events. The cause events occur in
an arbitrary order. Cause and effect events can be distributed over time, meaning they can 
be separated by 0 or more intervening events.

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
    java mona.causation.Main
        [-numEventTypes <quantity> (default=10)]
        [-numCauseEventTypes <quantity> (default=5)]
        [-numCausations <quantity> (default=2)]
        [-maxCauseEvents <quantity> (default=2)]
        [-maxInterveningEvents <quantity> (default=1)]
        [-causationInstanceLength <length> (default=6)]
        [-numCausationInstances <quantity> (default=10)]
        [-numNeurons <quantity> (default=128)]
        [-numEpochs <quantity> (default=500)]
        [-randomSeed <random number seed> (default=4517)]
  Print parameters:
    java mona.causation.Main -printParameters
  Version:
    java mona.causation.Main -version
Exit codes:
  0=success
  1=error
</pre>
