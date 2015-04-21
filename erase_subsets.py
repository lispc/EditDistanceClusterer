import sys
from collections import defaultdict
ss = []
sid = {}
def getsid(s):
    if not s in sid:
        sid[s] = len(ss) 
        ss.append(s)
    return sid[s]
sets = []
to_remove = set()
index = defaultdict(list)
local_set = set()
for l in open(sys.argv[1]):
    l = l.strip()
    if l.startswith('=== new'):
        local_set = set()
        continue
    if l.startswith('=== end'):
        sets.append(local_set)
        continue
    myid = getsid(l)
    index[myid].append(len(sets))
    local_set.add(myid)
for item in index:
    for small_setid in index[item]:
        if small_setid in to_remove:
            continue
        for big_setid in index[item]:
            if big_setid == small_setid:
                continue
            if len(sets[big_setid]) < len(sets[small_setid]):
                continue
            if sets[small_setid] <= sets[big_setid]:
                to_remove.add(small_setid)
for i in range(0, len(sets)):
    if i in to_remove:
        continue
    print '=== new cluster ==='
    for s in sorted([ss[k] for k in sets[i]]):
        print s
    print '=== end cluster ==='
