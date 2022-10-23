JFLAGS = -g
JC = javac
#JC = /usr/java/jdk1.8.0_45/bin/javac
JVM= java

BIN = ./exe/

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java -cp $(CCPATHS) -encoding iso-8859-1 -d $(BIN)

CCPATHS = ./src:$(JARS)
RCPATHS = $(BIN):$(JARS)

CLASSES = ./src/*.java

JARS = ./libraries/AIMA.jar:./libraries/CentralEnergia.jar

default: bin classes

bin:
	mkdir -p $(BIN)

classes: $(CLASSES:.java=.class)

clean:
	$(RM) -rf $(BIN)*
