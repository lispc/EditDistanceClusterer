set -e
ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/EditDistanceJoinerTest.java
time java -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceJoinerTest 2 testdata/join.big.in > testdata/join.big.out
if diff testdata/join.big.out testdata/join.big.std
then
    echo 'EditDistanceJoiner Test Succeed'
fi
