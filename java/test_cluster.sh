javac -cp lib/commons-io-2.4.jar -sourcepath src -d build/classes src/edu/tsinghua/dbgroup/*
java -cp lib/commons-io-2.4.jar:./build/classes edu.tsinghua.dbgroup.EditDistanceClustererTest 1 input.data > out
if diff out cluster.out
then
    echo 'test cluster succ'
fi
