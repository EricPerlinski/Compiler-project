#!/bin/sh
cd src
javac *.java model/*.java parser_tools/*.java  -d ../bin -cp "../antlr.jar"
