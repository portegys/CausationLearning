# Causation test results decision tree.

from matplotlib import pyplot as plt
from sklearn import datasets
from sklearn.tree import DecisionTreeClassifier 
from sklearn import tree
from sklearn import metrics
from sklearn.tree import export_graphviz
from sklearn.externals.six import StringIO 
import pandas
import sys, getopt
from IPython.display import Image
import pydotplus

# Get options
input_filename = ''
output_filename = 'causation_test_results_decision_tree.png'
usage = 'causation_test_results_decision_tree.py -i <input file name> (csv) [-o <output file name> (png)]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"?i:o:",["input=","output="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt in ("-?", "--help"):
     print(usage)
     sys.exit(0)
  if opt in ("-i", "--input"):
     input_filename = arg
  elif opt in ("-o", "--output"):
     output_filename = arg
  else:
     print(usage)
     sys.exit(1)
if input_filename == '':
  print(usage)
  sys.exit(1)

# Read test results dataset
dataset = pandas.read_csv(input_filename)
dataset = dataset.rename(columns={'NUM_EVENT_TYPES': 'NET','NUM_CAUSATIONS': 'NC','MAX_CAUSE_EVENTS': 'MCE','MAX_INTERVENING_EVENTS': 'MIE', 'test_pct': 'Score'})
X_names = ['NET','NC','MCE','MIE']
y_name = ['Score']

# Average accuracy for runs with same parameters
X_ave = dataset.filter(items=X_names).drop_duplicates()
X_ave[['Score']] = 0
X_ave = X_ave.reset_index()
for index, row in X_ave.iterrows():
  scores = dataset.loc[dataset['NET'] == row['NET']]
  scores = scores.loc[dataset['NC'] == row['NC']]
  scores = scores.loc[dataset['MCE'] == row['MCE']]
  scores = scores.loc[dataset['MIE'] == row['MIE']]['Score']
  if len(scores) > 0:
    row['Score'] = sum(scores) / len(scores)
dataset = X_ave
X = dataset[X_names]
y = dataset[y_name]

# Score as Good, Fair, Poor
good_count = 0
fair_count = 0
poor_count = 0

def score(x):
  global good_count, fair_count, poor_count
  if float(x) >= 90.0:
    good_count += 1
    return 'Good'
  elif float(x) >= 70.0:
    fair_count += 1
    return 'Fair'
  else:
    poor_count += 1
    return 'Poor'

y_scores = [score(i) for i in dataset['Score']]
dataset['Score'] = y_scores
y = dataset['Score']
print('Counts: ', 'good=', good_count, ',fair=', fair_count, ',poor=', poor_count, sep='')

# Fit the classifier with default hyper-parameters
clf = DecisionTreeClassifier(random_state=1234)
model = clf.fit(X, y)

# Accuracy
y_pred = model.predict(X)
print("Accuracy:",metrics.accuracy_score(y, y_pred))

# Text results
print('Text results:')
text_representation = tree.export_text(clf, feature_names=['NUM_EVENT_TYPES','NUM_CAUSATIONS','MAX_CAUSE_EVENTS','MAX_INTERVENING_EVENTS'])
print(text_representation)

# Graphical results
print('Writing graphical results to', output_filename)
dot_data = StringIO()
export_graphviz(clf, out_file=dot_data,
                filled=True, rounded=True, impurity=False,
                special_characters=True,
                feature_names = X_names,
                class_names=['Good', 'Fair', 'Poor'])
graph = pydotplus.graph_from_dot_data(dot_data.getvalue())
graph.write_png(output_filename)
Image(graph.create_png())
