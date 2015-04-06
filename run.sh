#!/bin/sh
mkdir -p tree/input
mkdir -p tree/output
mkdir -p tds/input
mkdir -p tds/output

java -classpath ".:antlr.jar:bin" plic.Test
