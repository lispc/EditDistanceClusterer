from collections import defaultdict
import sys
cnt = 0
results = [] #defaultdict(list)
fname = 'author.out'
if len(sys.argv) == 2:
    fname = sys.argv[1]
id1 = ""
id2 = ""
str1 = ""
str2 = ""
for l in open(fname):
    cnt += 1
    if cnt % 4 == 1:
        id1, id2 = l.strip().split()
    if cnt % 4 == 2:
        str1 = l.strip()
    if cnt % 4 == 3:
        results.append((int(id1)-1, int(id2)-1, str1 + " " + l.strip()))
results.sort()
for i1, i2, l in results:
    print l
