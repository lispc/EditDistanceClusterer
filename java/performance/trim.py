from collections import defaultdict
import sys
cnt = 0
results = [] #defaultdict(list)
fname = 'author.out'
if len(sys.argv) == 2:
    fname = sys.argv[1]
for l in open(fname):
    cnt += 1
    if cnt % 4 == 1:
        id1, id2 = l.strip().split()
        results.append((int(id1)-1, int(id2)-1))
results.sort()
for id1, id2 in results:
    print id1, id2
