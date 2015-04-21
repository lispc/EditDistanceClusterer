ant
mkdir -p build/sample_classes
javac -cp lib/commons-io-2.4.jar:lib/vicino-1.1.jar:./build/jar/EditDistanceJoiner.jar -sourcepath sample -d build/sample_classes sample/edu/tsinghua/dbgroup/sample/VicinoTester.java
time java -cp lib/commons-io-2.4.jar:lib/vicino-1.1.jar:lib/secondstring-20100303.jar:./build/sample_classes edu.tsinghua.dbgroup.sample.VicinoTester 2 performance/author.data > vicino_out
