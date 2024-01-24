# Test causation learning performance.

if [ "$1" = "" ]
then
   echo "Usage: causation_test.sh <number of runs>"
   exit 1
fi
runs=$1


Usage:
  Run:
    java mona.causation.CausationLearning
        [-numEventTypes <quantity> (default=10)]
        [-numCauseEventTypes <quantity> (default=5)]
        [-numCausations <quantity> (default=2)]
        [-maxCauseEvents <quantity> (default=2)]
        [-maxInterveningEvents <quantity> (default=2)]
        [-maxValidInterveningEvents <quantity> (default=1)]
        [-numValidTrainingCausationInstances <quantity> (default=5)]
        [-numInvalidTrainingCausationInstances <quantity> (default=5)]
        [-numValidTestingCausationInstances <quantity> (default=5)]
        [-numInvalidTestingCausationInstances <quantity> (default=5)]
        [-learner
           "LSTM" | "SimpleRNN" | "Attention" | "NN" |
             [-numHiddenNeurons <quantity> (default=128) (repeat for additional layers)]
             [-numEpochs <quantity> (default=500)]
           "GA" |
             [-generations <quantity> (default=10)]
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

Parameters:
NUM_EVENT_TYPES = 10
NUM_CAUSE_EVENT_TYPES = 5
NUM_CAUSATIONS = 2
MAX_CAUSE_EVENTS = 2
MAX_INTERVENING_EVENTS = 2
MAX_VALID_INTERVENING_EVENTS = 1
CAUSATION_INSTANCE_LENGTH = 6
NUM_VALID_TRAINING_CAUSATION_INSTANCES = 5
NUM_INVALID_TRAINING_CAUSATION_INSTANCES = 5
NUM_VALID_TESTING_CAUSATION_INSTANCES = 5
NUM_INVALID_TESTING_CAUSATION_INSTANCES = 5
LEARNER = LSTM
NUM_HIDDEN_NEURONS = {128}
NUM_EPOCHS = 500
GENERATIONS = 10
POPULATION_SIZE = 20
FIT_POPULATION_SIZE = 10
MUTATION_RATE = 0.25
RANDOM_SEED = 4517

echo basePathLength,numModularPaths,numNeurons,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_nn_test_results.csv
echo basePathLength,numModularPaths,numNeurons,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_nn_dilate_overlay_test_results.csv
echo basePathLength,numModularPaths,numNeurons,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_nn_dilate_accumulate_test_results.csv
echo basePathLength,numModularPaths,numNeurons,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_nn_dilate_normalize_test_results.csv
echo basePathLength,numModularPaths,numNeurons,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_rnn_test_results.csv
echo basePathLength,numModularPaths,kernelSize,pathModificationType,train_prediction_errors,train_total_predictions,train_error_pct,test_prediction_errors,test_total_predictions,test_error_pct > world_composer_tcn_test_results.csv

for basePathLength in $(seq $minBasePathLength $incrBasePathLength $maxBasePathLength)
do
 for numModularPaths in $(seq $minNumModularPaths $incrNumModularPaths $maxNumModularPaths)
 do
  for kernelSize in $(seq $minKernelSize $incrKernelSize $maxKernelSize)
  do
    for i in $(seq $runs)
    do
     numInsertionTestPaths=$(( $basePathLength / 2 ))
     numSubstitutionTestPaths=$(( $basePathLength / 2 ))
     numDeletionTestPaths=$(( $basePathLength / 2 ))
     randomSeed=$RANDOM
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths 0 -numDeletionTestPaths 0 -randomSeed $randomSeed
     python world_path_tcn.py --kernel_size $kernelSize
     echo ${basePathLength},${numModularPaths},${kernelSize},insert,$(cat world_path_tcn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_tcn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths 0 -randomSeed $randomSeed
     python world_path_tcn.py --kernel_size $kernelSize
     echo ${basePathLength},${numModularPaths},${kernelSize},substitute,$(cat world_path_tcn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_tcn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths 0 -numDeletionTestPaths $numDeletionTestPaths -randomSeed $randomSeed
     python world_path_tcn.py --kernel_size $kernelSize
     echo ${basePathLength},${numModularPaths},${kernelSize},delete,$(cat world_path_tcn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_tcn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths $numDeletionTestPaths -randomSeed $randomSeed
     python world_path_tcn.py --kernel_size $kernelSize
     echo ${basePathLength},${numModularPaths},${kernelSize},all,$(cat world_path_tcn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_tcn_test_results.csv
    done
  done
 done
done

for basePathLength in $(seq $minBasePathLength $incrBasePathLength $maxBasePathLength)
do
 for numModularPaths in $(seq $minNumModularPaths $incrNumModularPaths $maxNumModularPaths)
 do
  for numNeurons in $(seq $minNumNeurons $incrNumNeurons $maxNumNeurons)
  do
    for i in $(seq $runs)
    do
     numInsertionTestPaths=$(( $basePathLength / 2 ))
     numSubstitutionTestPaths=$(( $basePathLength / 2 ))
     numDeletionTestPaths=$(( $basePathLength / 2 ))
     randomSeed=$RANDOM

     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths 0 -numDeletionTestPaths 0 -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},insert,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_test_results.csv
     python world_path_rnn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},insert,$(cat world_path_rnn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_rnn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths 0 -numDeletionTestPaths 0 -dilateEvents overlay -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},insert,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_overlay_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths 0 -numDeletionTestPaths 0 -dilateEvents accumulate -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},insert,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_accumulate_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths 0 -numDeletionTestPaths 0 -dilateEvents normalize -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},insert,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_normalize_test_results.csv

     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths 0 -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},substitute,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_test_results.csv
     python world_path_rnn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},substitute,$(cat world_path_rnn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_rnn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths 0 -dilateEvents overlay -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},substitute,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_overlay_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths 0 -dilateEvents accumulate -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},substitute,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_accumulate_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths 0 -dilateEvents normalize -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},substitute,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_normalize_test_results.csv

     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths 0 -numDeletionTestPaths $numDeletionTestPaths -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},delete,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_test_results.csv
     python world_path_rnn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},delete,$(cat world_path_rnn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_rnn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths 0 -numDeletionTestPaths $numDeletionTestPaths -dilateEvents overlay -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},delete,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_overlay_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths 0 -numDeletionTestPaths $numDeletionTestPaths -dilateEvents accumulate -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},delete,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_accumulate_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths 0 -numSubstitutionTestPaths 0 -numDeletionTestPaths $numDeletionTestPaths -dilateEvents normalize -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},delete,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_normalize_test_results.csv

     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths $numDeletionTestPaths -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},all,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_test_results.csv
     python world_path_rnn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},all,$(cat world_path_rnn_results.json | cut -d'"' -f12,16,20,32,36,40 | sed 's/"/,/g') >> world_composer_rnn_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths $numDeletionTestPaths -dilateEvents overlay -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},all,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_overlay_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths $numDeletionTestPaths -dilateEvents accumulate -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},all,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_accumulate_test_results.csv
     ./world_composer.sh -basePathLength $basePathLength -minModularPathLength $minModularPathLength -maxModularPathLength $maxModularPathLength -numModularPaths $numModularPaths -numInsertionTestPaths $numInsertionTestPaths -numSubstitutionTestPaths $numSubstitutionTestPaths -numDeletionTestPaths $numDeletionTestPaths -dilateEvents normalize -randomSeed $randomSeed
     python world_path_nn.py --neurons $numNeurons
     echo ${basePathLength},${numModularPaths},${numNeurons},all,$(cat world_path_nn_results.json | cut -d'"' -f4,8,12,16,20,24 | sed 's/"/,/g') >> world_composer_nn_dilate_normalize_test_results.csv

    done
  done
 done
done

exit 0
