Welcome to the DSI Utilities!
-----------------------------

[The DSI Utilities](http://dsiutils.di.unimi.it/) are a mishmash of
classes accumulated during the last twenty years in projects developed at
the DSI (Dipartimento di Scienze dell'Informazione, i.e., Information
Sciences Department), now DI (Dipartimento di Informatica, i.e.,
Informatics Department), of the Università degli Studi di Milano.

Building
--------

Uses [Apache Maven](https://maven.apache.org/) but you need Java 1.8+ only, project uses Maven Wrapper (no need to install anything).
Btw, my own preferred way to install Java and Maven as well is [SDKMAN](https://sdkman.io/).

To build:
```
./mvnw clean verify
```

To build with slow tests also:
```
./mvnw clean verify -P slow
```

To "quick-build" (produce all artifacts but skip all test execution -- `-Dtest=void` matches no UT/IT to run):
```
./mvnw clean verify -Dtest=void -P vigna-release
```

Available profiles:
* vigna-release -- used by release plugin (adds sources, javadoc, gpg, skip tests)
* slow -- runs "slow tests" as part of build as well

