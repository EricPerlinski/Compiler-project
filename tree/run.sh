#!/bin/sh

inputs=`ls input`

for f in $inputs
do
	java Tree2img $f
	dot -Tjpg "output/"$f".dot" -o "output/"$f".jpg"
done

