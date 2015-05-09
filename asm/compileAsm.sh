#!/bin/sh

for i in `ls src`
do
	echo "\n****************************"
	echo "Fichier "$i
	echo "****************************\n"
	java -jar ../microPIUP4.jar -ass src/$i
done

mv src/*.iup bin

