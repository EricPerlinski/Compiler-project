#!/bin/sh
cd src
javac plic/*.java model/*.java parser_tools/*.java dot/*.java -d ../bin -cp "../antlr.jar"
