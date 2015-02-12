#!/bin/sh

if [ $# -eq 0 ] || [ $1 = '-c' -a $# -eq 1 ] || [ $1 = '-a' -a $# -eq 1 ]
then
	list=`ls "test/correct/"`
	for file in $list
	do
		echo "\033[32mFILE "$file"\033[0m\n"
		./run.sh < "test/correct/"$file
	done

fi
if [ $# -eq 1 ] && [ $# -eq 1 -a $1 = '-i' ] || [ $# -eq 1 -a $1 = '-a' ]
then
	list=`ls "test/incorrect/"`
	for file in $list
	do
		echo "\033[31mFILE "$file"\033[0m\n"
		./run.sh < "test/incorrect/"$file
	done
fi


