#!/bin/sh
cd src
javac *.java model/*.java parser_tools/*.java dot/*.java -d ../bin -cp "../antlr.jar"
