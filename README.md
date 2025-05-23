# Welcome to the DSI Utilities!

## Introduction

The DSI utilities are a mishmash of classes accumulated during the last
twenty years in projects developed at the DSI (Dipartimento di Scienze
dell'Informazione, e.g., Information Sciences Department), now DI
(Dipartimento di Informatica, i.e., Informatics Department), of the
Università degli Studi di Milano.

The DSI utilities are free software distributed under either the [GNU
Lesser General Public License
2.1+](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) or the
[Apache Software License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Building

You need [Ant](https://ant.apache.org/) and [Ivy](https://ant.apache.org/ivy/).
Then, run `ant ivy-setupjars jar`.

## Papers

* A [paper](http://vigna.di.unimi.it/papers.php#BoVMSJ) about the
  high-performance reimplementation of strings provided by the versatile
  class
  [`MutableString`](https://github.com/vigna/dsiutils/blob/master/src/it/unimi/dsi/lang/MutableString.java),
  and _compact approximators_, the randomized data structure used in
  [`TextPattern`](https://github.com/vigna/dsiutils/blob/master/src/it/unimi/dsi/util/TextPattern.java)
  to represent bad-character shifts.

* A [paper](http://vigna.di.unimi.it/papers.php#VigBIRSQ) about the
  broadword implementation of select queries implemented in
  [`Fast.select()`](https://github.com/vigna/dsiutils/blob/master/src/it/unimi/dsi/bits/Fast.java).

* Papers about the [pseudorandom number
  generators](http://prng.di.unimi.it/) included can be found
  [here](http://vigna.di.unimi.it/papers.php).
