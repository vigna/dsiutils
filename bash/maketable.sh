#!/bin/bash

# Takes the output of RandomSpeed and generates lines for an HTML table

repeats=4
numgens=5

tot=$(grep "ThreadLocalRandom.next" $1 | cut -d. -f2 | wc -l)
n=$((tot/repeats))

paste <(for((i=0; i<n; i++)); do echo " * <TR><TD>"; done) \
		<(grep "ThreadLocalRandom.next" $1 | cut -d. -f3 | tail -n $n) \
		<(egrep -o "[0-9.]+ ns"  $1 | cut -d' ' -f1 | tail -n $((n*numgens)) | awk "{ printf(\"<TD>%s\", \$0); i = i + 1; if ( i % $numgens == 0 ) printf \"\\n\"; }") 
