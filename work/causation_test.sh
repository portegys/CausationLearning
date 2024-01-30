# Test causation learning performance.

if [ "$1" = "" ]
then
   echo "Usage: causation_test.sh <number of runs>"
   exit 1
fi
runs=$1
if [ "`which jq 2>/dev/null`" = "" ]
then
   echo "jq command not found"
   exit 1
fi

# Data sizes
NUM_VALID_TRAINING_CAUSATION_INSTANCES=50
NUM_INVALID_TRAINING_CAUSATION_INSTANCES=50
NUM_VALID_TESTING_CAUSATION_INSTANCES=25
NUM_INVALID_TESTING_CAUSATION_INSTANCES=25
echo NUM_VALID_TRAINING_CAUSATION_INSTANCES=$NUM_VALID_TRAINING_CAUSATION_INSTANCES
echo NUM_INVALID_TRAINING_CAUSATION_INSTANCES=$NUM_INVALID_TRAINING_CAUSATION_INSTANCES
echo NUM_VALID_TESTING_CAUSATION_INSTANCES=$NUM_VALID_TESTING_CAUSATION_INSTANCES
echo NUM_INVALID_TESTING_CAUSATION_INSTANCES=$NUM_INVALID_TESTING_CAUSATION_INSTANCES

# Parameters:
MIN_NUM_EVENT_TYPES=15
INCR_NUM_EVENT_TYPES=5
MAX_NUM_EVENT_TYPES=25
MIN_NUM_CAUSATIONS=2
INCR_NUM_CAUSATIONS=4
MAX_NUM_CAUSATIONS=10
MIN_MAX_CAUSE_EVENTS=1
INCR_MAX_CAUSE_EVENTS=1
MAX_MAX_CAUSE_EVENTS=3
MIN_MAX_INTERVENING_EVENTS=0
INCR_MAX_INTERVENING_EVENTS=1
MAX_MAX_INTERVENING_EVENTS=2

echo NUM_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_lstm_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_simple_rnn_test_results.csv
echo NUM_EVENT_TYPES,NNUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_attention_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_nn_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_ga_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_histogram_test_results.csv

for NUM_EVENT_TYPES in $(seq $MIN_NUM_EVENT_TYPES $INCR_NUM_EVENT_TYPES $MAX_NUM_EVENT_TYPES)
do
 for NUM_CAUSATIONS in $(seq $MIN_NUM_CAUSATIONS $INCR_NUM_CAUSATIONS $MAX_NUM_CAUSATIONS)
 do
  for MAX_CAUSE_EVENTS in $(seq $MIN_MAX_CAUSE_EVENTS $INCR_MAX_CAUSE_EVENTS $MAX_MAX_CAUSE_EVENTS)
  do
   for MAX_INTERVENING_EVENTS in $(seq $MIN_MAX_INTERVENING_EVENTS $INCR_MAX_INTERVENING_EVENTS $MAX_MAX_INTERVENING_EVENTS)
   do
      echo NUM_EVENT_TYPES=${NUM_EVENT_TYPES} NUM_CAUSATIONS=${NUM_CAUSATIONS} MAX_CAUSE_EVENTS=${MAX_CAUSE_EVENTS} MAX_INTERVENING_EVENTS=${MAX_INTERVENING_EVENTS}
      random_list=""
      for i in $(seq $runs)
      do
       random_list="$random_list $RANDOM"
      done
      echo learner=LSTM
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner LSTM \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_rnn_results.json | tr -d '\r' | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_rnn_results.json | tr -d '\r' | jq -r .train_total_predictions`
       train_pct=`cat causation_rnn_results.json | tr -d '\r' | jq -r .train_pct`
       test_correct_predictions=`cat causation_rnn_results.json | tr -d '\r' | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_rnn_results.json | tr -d '\r' | jq -r .test_total_predictions`
       test_pct=`cat causation_rnn_results.json | tr -d '\r' | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_lstm_test_results.csv
       run=$((run + 1))
      done
      echo learner=SimpleRNN
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner SimpleRNN \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_rnn_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_rnn_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_rnn_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_rnn_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_rnn_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_rnn_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_simple_rnn_test_results.csv
       run=$((run + 1))
      done
      echo learner=Attention
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner Attention \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_attention_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_attention_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_attention_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_attention_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_attention_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_attention_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_attention_test_results.csv
       run=$((run + 1))
      done
      echo learner=NN
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner NN \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_nn_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_nn_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_nn_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_nn_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_nn_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_nn_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_nn_test_results.csv
       run=$((run + 1))
      done
      echo learner=GA
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner GA \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_ga_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_ga_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_ga_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_ga_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_ga_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_ga_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_ga_test_results.csv
       run=$((run + 1))
      done
      echo learner=Histogram
      run=1
      for randomSeed in $random_list
      do
       echo run=${run} randomSeed=$randomSeed
       echo Command line:
       cmd=`echo ./causation.sh \
        -learner Histogram \
        -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -randomSeed $randomSeed -verbose false`
       echo $cmd
       $cmd
       train_correct_predictions=`cat causation_histogram_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_histogram_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_histogram_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_histogram_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_histogram_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_histogram_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_histogram_test_results.csv
       run=$((run + 1))
      done
   done
  done
 done
done

exit 0
