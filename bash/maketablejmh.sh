#!/bin/bash

# for m in nextInt nextLong nextDouble nextInt100000 nextInt229228 nextInt230 nextInt2301 nextInt230229 nextLong1000000000000 nextLong2621; do
for m in nextLong nextInt100000 nextDouble; do
	for r in Random ThreadLocalRandom SplittableRandom SplitMix64 XoRoShiRo128PlusPlus XoRoShiRo128StarStar XoRoShiRo128Plus XoShiRo256PlusPlus XoShiRo256StarStar XoShiRo256Plus XorShift1024StarPhi; do
		v=$(grep ^Benchmark$r.$m\  $1 | tr -s ' ' | cut -d' ' -f4)
		r=$(grep ^Benchmark$r.$m\  $1 | tr -s ' ' | cut -d' ' -f6)
		echo -n "<TD STYLE='text-align: right'>$(echo $v-$r-1 | bc -l)"
	done
	echo
done
