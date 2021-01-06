#!/bin/bash -e

if [[ "$@" == "" ]]; then
	echo "USAGE: $(basename $0) FCL" 1>&2
	echo "The list of string will be read from standard input in UTF-8 encoding." 1>&2
	exit 1
fi

PERM=$(mktemp)
LEXFCL=$(mktemp)

nl -v0 -nln | LC_ALL=C sort -S2G -T. -k2 | tee >(cut -f1 | tr -d ' ' >$PERM) | cut -f2 | java -server it.unimi.dsi.util.FrontCodedStringList -u $LEXFCL

java -server it.unimi.dsi.util.PermutedFrontCodedStringList -i -t $LEXFCL $PERM $1

rm -f $LEXFCL $PERM
