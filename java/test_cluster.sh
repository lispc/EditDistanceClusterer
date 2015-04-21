set -e
ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:lib/vicino-1.1.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/*
time java -cp lib/commons-io-2.4.jar:./build/jar/EditDistanceJoiner.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.EditDistanceClustererTest 2 performance/author.data > out
#if diff out cluster.out
#then
#    echo 'test cluster succ'
#fi
