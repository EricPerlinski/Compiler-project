#!/bin/sh
cd src
java -jar ../antlr.jar Plic.g -o plic 

sed -i '1ipackage plic;' plic/*.java
