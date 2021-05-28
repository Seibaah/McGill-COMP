#!/bin/bash

path=$1 
partPath=$(dirname -- "$1")
partPath+=$(basename -- "$1")
fullPath=$(echo "$partPath" | tr '/' '_')
mkdir -p ./tempStack
find "$1" -name *.jpg -exec cp -p '{}' ./tempStack \;
convert -append $(ls -tr ./tempStack/*.jpg) "$fullPath".jpg
rm -r ./tempStack

#We store the 1st argument in a variable

#Using dirnamee and basename we can get a string of the full path given by the 1st argument
#	Using tr we replace the '/' by '_' as asked in the Question 3
#	We'll use this string later to name the output appended image file
#We make a temporary directory in our pwd to copy. -p so it makes necessary parent directory as needed.

#We use find with the argument as path and go look for all the .jpg files in said directory.
#	-exec will execute the commands until '\;' after it encounters the first .jpg
#	Basically it creates a subroutine for find to allo us to do something with the file it has found.
#	In exec we use cp to copy the .jpg to our recently created temporary directory. -p is important
#	as it'll preserve the file timestamp 

#We use convert -append to append images vertically. What follows is a path to our temporary directory referring to
#	all the .jpg files. We use ls -tr to list them chronologically from oldest to newest.
#	The second part is the creation of the outut file in our pwd. Now we use the string we first created to name it 
#	similarly to the path provided in the first argument
#
#We remove our temporary directory and all of it's content.
