set -e
ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/EditDistanceJoinerTest.java
time java -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceJoinerTest 2 data/join.big.in > data/join.big.out
if diff data/join.big.out data/join.big.std
then
    echo 'EditDistanceJoiner Test Succeed'
fi
