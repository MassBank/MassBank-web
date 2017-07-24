#----------------------------------------------------------
# Search.cgi build script for Linux
#----------------------------------------------------------

#
# Usage: 
#	make
# or	make clean
#

CXXFLAGS=-I/usr/include/mysql -O3 -Wall 

all: Search.cgi

Search.cgi: search.o
	$(CXX) -o Search.cgi search.o -lmysqlclient

search.o: src/search.cpp src/search.h
	$(CXX) $(CXXFLAGS) -c -o search.o src/search.cpp 

clean:
	rm -f search.o Search.cgi
