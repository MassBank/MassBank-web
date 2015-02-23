@echo off
REM ----------------------------------------------------------
REM Search.cgi build script for Windows
REM ----------------------------------------------------------

@echo on
C:\MinGW\bin\g++ -I"C:\MinGW\include" -I./include -O2 -Wall -c -osearch.o src/search.cpp
C:\MinGW\bin\g++ -L./ -oSearch.cgi search.o libmysql.lib
pause
