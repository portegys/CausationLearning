# Test causation learning performance.

if [ "$1" = "" ]
then
   echo "Usage: causation_test.sh <number of runs>"
   exit 1
fi
runs=$1

# Data sizes
NUM_VALID_TRAINING_CAUSATION_INSTANCES=5
NUM_INVALID_TRAINING_CAUSATION_INSTANCES=5
NUM_VALID_TESTING_CAUSATION_INSTANCES=5
NUM_INVALID_TESTING_CAUSATION_INSTANCES=5

# Parameters:
MIN_NUM_EVENT_TYPES=10
MAX_NUM_EVENT_TYPES=20
INCR_NUM_EVENT_TYPES=5
MIN_NUM_CAUSE_EVENT_TYPES=5
INCR_NUM_CAUSE_EVENT_TYPES=5
MAX_NUM_CAUSE_EVENT_TYPES=$MIN_NUM_EVENT_TYPES
MIN_NUM_CAUSATIONS=2
INCR_NUM_CAUSATIONS=4
MAX_NUM_CAUSATIONS=10
MIN_MAX_CAUSE_EVENTS=2
INCR_MAX_CAUSE_EVENTS=4
MAX_MAX_CAUSE_EVENTS=10
MIN_MAX_INTERVENING_EVENTS=2
INCR_MAX_INTERVENING_EVENTS=2
MAX_MAX_INTERVENING_EVENTS=4
MIN_MAX_VALID_INTERVENING_EVENTS=0
INCR_MAX_VALID_INTERVENING_EVENTS=1
MAX_MAX_VALID_INTERVENING_EVENTS=$MIN_MAX_INTERVENING_EVENTS

echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_lstm_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_simple_rnn_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_attention_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_nn_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_ga_test_results.csv
echo NUM_EVENT_TYPES,NUM_CAUSE_EVENT_TYPES,NUM_CAUSATIONS,MAX_CAUSE_EVENTS,MAX_INTERVENING_EVENTS,MAX_VALID_INTERVENING_EVENTS,train_correct_predictions,train_total_predictions,train_pct,test_correct_predictions,test_total_predictions,test_pct > causation_learning_histogram_test_results.csv

for NUM_EVENT_TYPES in $(seq $MIN_NUM_EVENT_TYPES $INCR_NUM_EVENT_TYPES $MAX_NUM_EVENT_TYPES)
do
 for NUM_CAUSE_EVENT_TYPES in $(seq $MIN_NUM_CAUSE_EVENT_TYPES $INCR_NUM_CAUSE_EVENT_TYPES $MAX_NUM_CAUSE_EVENT_TYPES)
 do
  for NUM_CAUSATIONS in $(seq $MIN_NUM_CAUSATIONS $INCR_NUM_CAUSATIONS $MAX_NUM_CAUSATIONS)
  do
   for MAX_CAUSE_EVENTS in $(seq $MIN_MAX_CAUSE_EVENTS $INCR_MAX_CAUSE_EVENTS $MAX_MAX_CAUSE_EVENTS)
   do
    for MAX_INTERVENING_EVENTS in $(seq $MIN_MAX_INTERVENING_EVENTS $INCR_MAX_INTERVENING_EVENTS $MAX_MAX_INTERVENING_EVENTS)
    do
     for MAX_VALID_INTERVENING_EVENTS in $(seq $MIN_MAX_VALID_INTERVENING_EVENTS $INCR_MAX_VALID_INTERVENING_EVENTS $MAX_MAX_VALID_INTERVENING_EVENTS)
     do
      for i in $(seq $runs)
      do
       randomSeed=$RANDOM
       ./causation.sh -learner LSTM -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_rnn_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_rnn_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_rnn_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_rnn_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_rnn_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_rnn_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_lstm_test_results.csv
      done
      for i in $(seq $runs)
      do
       ./causation.sh -learner SimpleRNN -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_rnn_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_rnn_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_rnn_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_rnn_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_rnn_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_rnn_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_simple_rnn_test_results.csv
      done
      for i in $(seq $runs)
      do
       ./causation.sh -learner Attention -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_attention_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_attention_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_attention_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_attention_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_attention_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_attention_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_attention_test_results.csv
      done
      for i in $(seq $runs)
      do
       ./causation.sh -learner NN -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_nn_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_nn_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_nn_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_nn_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_nn_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_nn_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_nn_test_results.csv
      done
      for i in $(seq $runs)
      do
       ./causation.sh -learner GA -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_ga_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_ga_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_ga_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_ga_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_ga_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_ga_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_ga_test_results.csv
      done
      for i in $(seq $runs)
      do
       ./causation.sh -learner Histogram -numValidTrainingCausationInstances $NUM_VALID_TRAINING_CAUSATION_INSTANCES \
        -numInvalidTrainingCausationInstances $NUM_INVALID_TRAINING_CAUSATION_INSTANCES \
        -numValidTestingCausationInstances $NUM_VALID_TESTING_CAUSATION_INSTANCES \
        -numInvalidTestingCausationInstances $NUM_INVALID_TESTING_CAUSATION_INSTANCES \
        -numEventTypes $NUM_EVENT_TYPES \
        -numCauseEventTypes $NUM_CAUSE_EVENT_TYPES \
        -numCausations $NUM_CAUSATIONS \
        -maxCauseEvents $MAX_CAUSE_EVENTS \
        -maxInterveningEvents $MAX_INTERVENING_EVENTS \
        -maxValidInterveningEvents $MAX_VALID_INTERVENING_EVENTS \
        -randomSeed $randomSeed
       train_correct_predictions=`cat causation_histogram_results.json | jq -r .train_correct_predictions`
       train_total_predictions=`cat causation_histogram_results.json | jq -r .train_total_predictions`
       train_pct=`cat causation_histogram_results.json | jq -r .train_pct`
       test_correct_predictions=`cat causation_histogram_results.json | jq -r .test_correct_predictions`
       test_total_predictions=`cat causation_histogram_results.json | jq -r .test_total_predictions`
       test_pct=`cat causation_histogram_results.json | jq -r .test_pct`
       echo ${NUM_EVENT_TYPES},${NUM_CAUSE_EVENT_TYPES},${NUM_CAUSATIONS},${MAX_CAUSE_EVENTS},${MAX_INTERVENING_EVENTS},${MAX_VALID_INTERVENING_EVENTS},${train_correct_predictions},${train_total_predictions},${train_pct},${test_correct_predictions},${test_total_predictions},${test_pct} >> causation_learning_histogram_test_results.csv
      done
     done
    done
   done
  done
 done
done

exit 0
