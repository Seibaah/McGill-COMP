#!/bin/bash

codebook=$(cat $1)
cat $2 | tr "$codebook" 'a-z A-Z'

#store the 1st argument in a variable named codebook
#Using cat read what is in the 2nd argument and with piping use tr to translate from 
#	the old range (i.e what is in codebook) to the new one (alphabet positional equivalent)
#This will output the decyphered message.
