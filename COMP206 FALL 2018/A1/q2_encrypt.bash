#!/bin/bash
codebook=$(cat $1) 
cat $2 | tr 'a-z A-Z' "$codebook"

#Store the 1st argument in a variable named codebook
#Using cat read what is in the 2nd argument and with pipe and tr translate the old range (i.e alphabet) 
#	to the new encrypted equivalent. This will output the encrypted message.
