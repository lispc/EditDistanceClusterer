set -e
javac -cp lib/commons-io-2.4.jar -sourcepath src -d build/classes src/edu/tsinghua/dbgroup/*
time java -cp lib/commons-io-2.4.jar:./build/classes edu.tsinghua.dbgroup.EditDistanceJoinerTest 2 performance/author.data > tmp.out
if diff tmp.out performance/trim.txt
then
    echo 'test succ'
fi
