set -e
ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/*
time java -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceJoinerTest 2 performance/author.data > tmp.out
#time java -Xprof -cp lib/commons-io-2.4.jar:./build/classes edu.tsinghua.dbgroup.EditDistanceJoinerTest 2 performance/author.data > tmp.out
if diff tmp.out performance/trim.txt
then
    echo 'test succ'
fi
