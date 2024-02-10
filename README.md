# Causation learning.

Learn causations in event sequences. A causation sequence has one or more cause
events, occurring in an arbitrary order, with possible intervening non-causal events.

Several learning methods are available:<br>
LSTM: Long Short-Term Memory neural network.<br>
Simple RNN: Simple recurrent neural network.<br>
Attention: LSTM with attention layer.<br>
NN: Multilayer perceptron (non-recurrent).<br>
GA: Genetic algorithm.<br>
Histogram: Event histogram algorithm.<br>

One aim of this project is to build more generalized mediator neurons for the Mona neural network.
https://github.com/morphognosis/NestingBirds

Develop: import Eclipse project.

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
        [-numEventTypes &lt;quantity&gt; (default=10)]
        [-numCausations <quantity> (default=2)]
        [-maxCauseEvents <quantity> (default=2)]
        [-maxInterveningEvents <quantity> (default=1)]
        [-numValidTrainingCausationInstances <quantity> (default=5)]
        [-numInvalidTrainingCausationInstances <quantity> (default=5)]
        [-numValidTestingCausationInstances <quantity> (default=5)]
        [-numInvalidTestingCausationInstances <quantity> (default=5)]
        [-learner
           "LSTM" | "SimpleRNN" | "Attention" | "NN" |
             [-numHiddenNeurons <quantity> (default=128) (repeat for additional layers)]
             [-numEpochs <quantity> (default=500)]
           "GA" |
             [-generations <quantity> (default=100)]
             [-populationSize <quantity> (default=20)]
             [-fitPopulationSize <quantity> (default=10)]
             [-mutationRate <probability> (default=0.25)]
           "Histogram" (default=LSTM)]
        [-randomSeed <random number seed> (default=4517)]
        [-verbose "true" | "false" (default=true)]
  Print parameters:
    java mona.causation.CausationLearning -printParameters
  Version:
    java mona.causation.CausationLearning -version
Exit codes:
  0=success
  1=error
</pre>

Run output files:
<pre>
causations.json: causations.
causation_instances.json: causation instances.
causation_attention_dataset.py: attention dataset.
causation_attention_results.json: attention results.
causation_rnn_dataset.py: LSTM and SimpleRNN dataset.
causation_rnn_results.json: LSTM and SimpleRNN results.
causation_nn_dataset.py: NN dataset.
causation_nn_results.json: NN results.
causation_ga_results.json: GA results.
causation_histogram_results.json: Histogram results.
</pre>

Test all learners (requires jq command):
<pre>
causation_test.sh &lt;number of runs&gt;
</pre>

Testing results files:
<pre>
causation_learning_lstm_test_results.csv
causation_learning_simple_rnn_test_results.csv
causation_learning_attention_test_results.csv
causation_learning_nn_test_results.csv
causation_learning_ga_test_results.csv         
causation_learning_histogram_test_results.csv
</pre>

Testing results analysis:
<pre>
causation_test_results_decision_tree.py -i &lt;testing results csv file&gt;
  [-o &lt;testing results png file&gt; default: causation_test_results_decision_tree.png]
</pre>
