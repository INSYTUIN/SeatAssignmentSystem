@echo off
echo Compiling project...
javac -encoding UTF-8 -cp ".;lib/mysql-connector-j-9.4.0.jar" -d out src/*.java

echo.
echo Running program...
java -cp ".;lib/mysql-connector-j-9.4.0.jar;out" src.SeatAssignmentSystem

echo.
pause
