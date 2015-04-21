set -e
ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/EditDistanceClustererTest.java
java -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceClustererTest 1 data/cluster.small.in > data/cluster.small.out
if diff data/cluster.small.out data/cluster.small.std
then
    echo 'EditDistanceClusterer Test Succeed'
fi
