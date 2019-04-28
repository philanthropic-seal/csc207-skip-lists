Skip Lists Assignment
=====================

Contains code for a skip list including a set, get, and remove method as well as tests and complexity analysis experiments. 



When running the complexity analysis, we get:

set (1000): 49.832
get (1000): 50.564
remove (1000): 52.6

set (2000): 49.898
get (2000): 51.796
remove (2000): 53.182

set (4000): 49.968
get (4000): 51.442
remove (4000): 51.692

set (8000): 52.098
get (8000): 52.682
remove (8000): 53.835

set (16000): 53.508
get (16000): 53.078
remove (16000): 55.725

(extra indentation added for readability)

Running the analysis a few more times gives similar results (a range of 47-53 which grows to a range of 51-57). The growth is relatively even while all together, so it does appear to have a Big-O analysis of O(log n).
