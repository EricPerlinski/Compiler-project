#!/bin/sh

if [ $# -eq 0 ] || [ $1 = '-c' -a $# -eq 1 ]
then
	echo "CORRECT\n"
	list=`ls "test/correct/"`
	for file in $list
	do
		echo "\033[31mFILE "$file"\033[0m\n"
		./run.sh < "test/correct/"$file
	done

elif [ $1 = '-i' -a $# -eq 1 ]
then
	echo "INCORRECT\n"
	list=`ls "test/incorrect/"`
	for file in $list
	do
		echo "\033[31mFILE "$file"\033[0m\n"
		./run.sh < "test/incorrect/"$file
	done
fi


