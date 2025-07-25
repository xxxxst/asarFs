@echo off

set rootDir=%~dp0

if not "%1" == "test" (
	call %rootDir%\build.bat
)

set version=0.0.3
set mainClass=asarFsTest.TestMain
set encoding=utf8

set srcDir=%rootDir%\src\test
set outDir=%rootDir%\bin
set tagDir=%outDir%\test
set libDir=%rootDir%\lib

set lib=.;%tagDir%;%outDir%\asarFs-%version%.jar;%libDir%\org.json-1.6-20240205.jar;%libDir%\gson-2.11.0.jar
set src=%srcDir%/asarFsTest/*.java

if exist "%tagDir%" (
	rd /s /q "%tagDir%"
)

if not exist "%tagDir%" (
	mkdir "%tagDir%"
)

javac -encoding "%encoding%" -d "%tagDir%" -sourcepath "%srcDir%" -cp "%lib%" ""%src%""
java -cp "%lib%" "%mainClass%" -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
