#!/bin/bash

export LABEL=("nextLong()" "nextDouble()" "nextInt(100000)" "nextInt(2<sup>30</sup>+1)")
export i=0

for m in nextLong nextDouble nextInt100000 nextInt2301; do
	echo " * <TR>"
	echo " * <TH STYLE='text-align: left'><code>${LABEL[$i]}</code>"
	let i=i+1
	for r in Random ThreadLocalRandom SplittableRandom SplitMix64 XoRoShiRo128PlusPlus XoRoShiRo128StarStar XoRoShiRo128Plus XoShiRo256PlusPlus XoShiRo256StarStar XoShiRo256Plus XorShift1024StarPhi; do
		v=$(grep ^Benchmark$r.$m\  $1 | tr -s ' ' | cut -d' ' -f4)
		echo " * <TD STYLE='text-align: right'>$v"
	done
	echo " * "
done
