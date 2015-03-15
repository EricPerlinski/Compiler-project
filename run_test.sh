#!/bin/sh
echo "\n\033[33m===== Analyse fichiers plic ====\033[0m"
mkdir -p tree/input 

if [ $# -eq 0 ] || [ $1 = '-c' -a $# -eq 1 ] || [ $1 = '-a' -a $# -eq 1 ]
then
	list=`ls "test/correct/"`
	cd bin
	for file in $list
	do
		echo "\n\033[32mFILE "$file"\033[0m"
		java -classpath ".:../antlr.jar" Test "../test/correct/"$file # > "../tree/input/"$file.tree
	done
	cd ..
fi
if [ $# -eq 1 ] && [ $# -eq 1 -a $1 = '-i' ] || [ $# -eq 1 -a $1 = '-a' ]
then
	list=`ls "test/incorrect/"`
	cd bin
	for file in $list
	do
		echo "\n\033[31mFILE "$file"\033[0m"
		java -classpath ".:../antlr.jar" Test  "../test/incorrect/"$file # > "../tree/input/"$file.tree
	done
	cd ..
fi

break;


cd tree
#echo "\n\033[33m===== Compilation Tree2img =====\033[0m"
#javac Tree2img.java
#echo "ok"
echo "\n\033[33m========= Run Tree2img =========\033[0m"
sh run.sh


