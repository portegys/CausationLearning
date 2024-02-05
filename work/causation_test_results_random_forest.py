# Causation test results random forest.

from matplotlib import pyplot as plt
from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier
from sklearn import tree
from sklearn import metrics
import pandas
import graphviz
from sklearn.tree import export_graphviz
import sys, getopt

# Get options
input_filename = ''
output_filename = ''
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
dataset = dataset.rename(columns={"test_pct": "Score"})
y_name = ['Score']
X_names = ['NUM_EVENT_TYPES','NUM_CAUSATIONS','MAX_CAUSE_EVENTS','MAX_INTERVENING_EVENTS']
X = dataset[X_names]

# Score y values as A+, A, B, C, D, F
def score(x):
  if x == 100.0:
    return 'A+'
  elif float(x) >= 90.0:
    return 'A'
  elif float(x) >= 80.0:
    return 'B'
  elif float(x) >= 70.0:
    return 'C'
  elif float(x) >= 60.0:
    return 'D'
  else:
    return 'F'

y_scores = [score(i) for i in dataset['Score']]
dataset['Score'] = y_scores
y = dataset['Score']

# Model (can also use single decision tree)
model = RandomForestClassifier(n_estimators=10)

# Train
model.fit(X, y)

# Extract single tree
estimator = model.estimators_[5]

# Export as dot file
export_graphviz(estimator, out_file='random_forest.dot',
                feature_names = X_names,
                class_names = ['A+','A','B','C','D','F'],
                rounded = True, proportion = False, 
                precision = 2, filled = True)

# Convert to png using system command (requires Graphviz)
from subprocess import call
call(['dot', '-Tpng', 'random_forest.dot', '-o', 'random_forest.png', '-Gdpi=600'])

# Display in jupyter notebook
from IPython.display import Image
Image(filename = 'random_forest.png')
