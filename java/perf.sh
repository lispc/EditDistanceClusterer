javac -Xlint:unchecked -cp mylib/commons-io-2.4.jar Joiner.java
time java -cp mylib/commons-io-2.4.jar:. Joiner 2 ../../passjoin/tiny.data > java_tiny.out
if diff java_tiny.out ../../passjoin/tiny_trim.out
then
    echo 'test succ'
fi
