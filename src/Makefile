# NOTE: need to run setup jdk first, and run gmake

DIR = .
SRCS = $(wildcard $(DIR)/*.java)
OBJS = $(SRCS:.java=.class)

all:	$(OBJS)

clean: FORCE
	rm -f *.class core *~
	rm crawler_results/*.txt
	rm *.xls

.SUFFIXES: .java .class

.java.class:
	javac $<

FORCE:
