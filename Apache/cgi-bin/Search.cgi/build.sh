#!/bin/sh
#----------------------------------------------------------
# Search.cgi build script for Linux
#----------------------------------------------------------

g++ -I./include -O3 -Wall -c -osearch.o src/search.cpp
g++ -L./ -oSearch.cgi search.o libmysqlclient.so
