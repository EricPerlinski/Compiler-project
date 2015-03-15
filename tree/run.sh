#!/bin/sh

inputs=`ls input`

for f in $inputs
do
	#java Tree2img $f
	dot -Tjpg "input/"$f -o "output/"$f".jpg"
done

