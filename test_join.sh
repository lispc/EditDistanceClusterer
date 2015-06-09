set -e
ant
mkdir -p build/sample_classes
javac -cp ./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/EditDistanceJoinerTest.java
for thread in `seq 8 1 8`
do
    time java -cp ./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceJoinerTest 2 testdata/join.big.in testdata/join.big.out $thread
    if diff testdata/join.big.std testdata/join.big.out > testdata/join.big.diff
    then
        echo 'EditDistanceJoiner Test Succeed'
        echo '==============================='
    else
        echo 'EditDistanceJoiner Test Fail'
        exit -1
    fi
done
