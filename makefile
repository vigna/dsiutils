include build.properties

TAR=tar

source:
	rm -fr dsiutils-$(version)
	ant clean
	ln -s . dsiutils-$(version)
	$(TAR) chvf dsiutils-$(version)-src.tar --owner=0 --group=0 \
		dsiutils-$(version)/README.md \
		dsiutils-$(version)/CHANGES \
		dsiutils-$(version)/COPYING.LESSER \
		dsiutils-$(version)/LICENSE-2.0.txt \
		dsiutils-$(version)/build.xml \
		dsiutils-$(version)/ivy.xml \
		dsiutils-$(version)/dsiutils.bnd \
		dsiutils-$(version)/pom-model.xml \
		dsiutils-$(version)/build.properties \
		$$(find dsiutils-$(version)/src/it/unimi/dsi -iname \*.java -or -iname \*.html -or -iname \*.in.16 -or -iname \*.out.12) \
		$$(find dsiutils-$(version)/test/it/unimi/dsi -iname \*.java -or -iname \*.html -or -iname \*.data) \
		$$(find dsiutils-$(version)/slow/it/unimi/dsi -iname \*.java -or -iname \*.html) \
		dsiutils-$(version)/src/overview.html
	$(TAR) --delete --wildcards -v -f dsiutils-$(version)-src.tar \
		dsiutils-$(version)/src/it/unimi/dsi/test/*.java \
		dsiutils-$(version)/test/it/unimi/dsi/test/*.java \
		dsiutils-$(version)/src/it/unimi/dsi/util/IntParallel*.java \
		dsiutils-$(version)/src/it/unimi/dsi/util/XorGens*.java \
		dsiutils-$(version)/src/it/unimi/dsi/stat/Ziggurat.java 
	gzip -f dsiutils-$(version)-src.tar
	rm dsiutils-$(version)

binary:
	rm -fr dsiutils-$(version)
	$(TAR) zxvf dsiutils-$(version)-src.tar.gz
	(cd dsiutils-$(version) && unset CLASSPATH && unset LOCAL_IVY_SETTINGS && ant ivy-clean ivy-setupjars && ant junit && ant clean && ant jar javadoc)
	$(TAR) zcvf dsiutils-$(version)-bin.tar.gz --owner=0 --group=0 \
		dsiutils-$(version)/README.md \
		dsiutils-$(version)/CHANGES \
		dsiutils-$(version)/COPYING.LESSER \
		dsiutils-$(version)/LICENSE-2.0.txt \
		dsiutils-$(version)/dsiutils-$(version).jar \
		dsiutils-$(version)/docs
	$(TAR) zcvf dsiutils-$(version)-deps.tar.gz --owner=0 --group=0 --transform='s|.*/||' $$(find dsiutils-$(version)/jars/runtime -iname \*.jar -exec readlink {} \;) 

stage:
	rm -fr dsiutils-$(version)
	$(TAR) zxvf dsiutils-$(version)-src.tar.gz
	cp -fr bnd dsiutils-$(version)
	(cd dsiutils-$(version) && unset CLASSPATH && unset LOCAL_IVY_SETTINGS && ant ivy-clean ivy-setupjars && ant stage)
