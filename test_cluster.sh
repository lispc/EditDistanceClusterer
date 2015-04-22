set -e
ant
mkdir -p build/sample_classes
javac -cp ./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/EditDistanceClustererTest.java
java -cp ./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceClustererTest 1 testdata/cluster.small.in > testdata/cluster.small.out
if diff testdata/cluster.small.out testdata/cluster.small.std
then
    echo 'EditDistanceClusterer Test Succeed'
fi
