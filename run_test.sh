#!/bin/sh
echo "\n\033[33m===== Analyse fichiers plic ====\033[0m"
mkdir -p tree/input
mkdir -p tree/output
mkdir -p tds/input
mkdir -p tds/output
mkdir -p asm/src
mkdir -p asm/bin

if [ $# -eq 0 ] || [ $1 = '-c' -a $# -eq 1 ] || [ $1 = '-a' -a $# -eq 1 ]
then
	list=`ls "test/correct/"`
#	cd bin
	for file in $list
	do
		echo "\n\033[32mFILE "$file"\033[0m"
		java -classpath ".:antlr.jar:bin" plic.Test "./test/correct/"$file # > "./tree/input/"$file.tree
	done
#	cd ..
fi
if [ $# -eq 1 ] && [ $# -eq 1 -a $1 = '-i' ] || [ $# -eq 1 -a $1 = '-a' ]
then
	list=`ls "test/incorrect/"`
#	cd bin
	for file in $list
	do
		echo "\n\033[31mFILE "$file"\033[0m"
		java -classpath ".:antlr.jar:bin" plic.Test  "./test/incorrect/"$file # > "./tree/input/"$file.tree
	done
#	cd ..
fi
if [ $# -eq 1 ] && [ $# -eq 1 -a $1 = '-is' ] || [ $# -eq 1 -a $1 = '-a' ]
then
	list=`ls "test/incorrect_semantic/"`
#	cd bin
	for file in $list
	do
		echo "\n\033[31mFILE "$file"\033[0m"
		java -classpath ".:antlr.jar:bin" plic.Test  "./test/incorrect_semantic/"$file # > "./tree/input/"$file.tree
	done
#	cd ..
fi

break;



echo "\n\033[33m========= GENERATION TREE =========\033[0m"
cd tree
sh run.sh
echo "OK"
cd ..
echo "\n\033[33m========= GENERATION TDS =========\033[0m"
cd tds
sh run.sh
echo "OK"
cd ..
echo "\n\033[33m========= GENERATION IUP =========\033[0m"
cd asm
sh compileAsm.sh 
echo "OK"
cd ..
