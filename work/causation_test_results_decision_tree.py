# Causation test results decision tree.

from matplotlib import pyplot as plt
from sklearn import datasets
from sklearn.tree import DecisionTreeClassifier 
from sklearn import tree
from sklearn import metrics
import pandas
import graphviz
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

# Fit the classifier with default hyper-parameters
clf = DecisionTreeClassifier(random_state=1234)
model = clf.fit(X, y)

# Predict the response for test dataset
y_pred = model.predict(X)

# Model Accuracy, how often is the classifier correct?
print("Accuracy:",metrics.accuracy_score(y, y_pred))

# Text representation.
text_representation = tree.export_text(clf)
print(text_representation)

fig = plt.figure(figsize=(25,20))
_ = tree.plot_tree(clf, 
                   feature_names=X_names,
                   class_names=['A+','A','B','C','D','F'],
                   filled=True)

# DOT data
dot_data = tree.export_graphviz(clf, out_file=None,
                                feature_names=X_names,
                                class_names=['A+','A','B','C','D','F'],
                                filled=True)
print(dot_data)

# Draw graph
graph = graphviz.Source(dot_data, format="png")
graph

from sklearn.tree import export_graphviz
from sklearn.externals.six import StringIO  
from IPython.display import Image  
import pydotplus

dot_data = StringIO()
export_graphviz(clf, out_file=dot_data,  
                filled=True, rounded=True,
                special_characters=True,feature_names = X_names,class_names=['A+','A','B','C','D','F'])
graph = pydotplus.graph_from_dot_data(dot_data.getvalue())  
graph.write_png('decision_tree.png')
Image(graph.create_png())
