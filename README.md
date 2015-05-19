# EditDistanceJoiner

EditDistanceJoiner is a java library develop by [database group](http://dbgroup.cs.tsinghua.edu.cn/) of Tsinghua University which can help you (a) select similar string pairs and (b) get similar string clusters among lots of strings based on similarity measured by edit distance very effiently.

### How does it work?
This library is based on a method called [PassJoin](http://dbgroup.cs.tsinghua.edu.cn/dd/projects/passjoin/index.html) proposed on VLDB2012, which is proved to be orders of magnitude faster than previous methods. The library can handle a dataset in 2 minutes which costs 70 minutes by naive brute force implementation used in [simile-vicino](https://code.google.com/p/simile-vicino/), besides, unlike simile-vicino which uses blocking methods to speed up clustering with the loss of accuracy, this library can generate accurate results.

### Usage
This library use similar interface with simile-vicino. You can have a look at the samples in joining and clustering at [EditDistanceClustererTest](sample/edu/tsinghua/dbgroup/sample/EditDistanceClustererTest.java) and [EditDistanceJoinerTest](sample/edu/tsinghua/dbgroup/sample/EditDistanceJoinerTest.java)
