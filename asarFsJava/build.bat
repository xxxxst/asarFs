@echo off

set rootDir=%~dp0

set version=0.0.2
set encoding=utf8
set locale=en_US

set srcDir=%rootDir%\src\main
set outDir=%rootDir%\bin
set tagDir=%outDir%\main
set docDir=%outDir%\doc
set libDir=%rootDir%\lib

set lib=.;%tagDir%;%libDir%/org.json-1.6-20240205.jar
@REM set src=@sourcelist.txt
set src=%srcDir%/com/vcedit/asarFs/*.java

if exist "%tagDir%" (
	rd /s /q "%tagDir%"
)

if exist "%docDir%" (
	rd /s /q "%docDir%"
)

if not exist "%tagDir%" (
	mkdir "%tagDir%"
)

del "%outDir%\*.jar"

javac -encoding "%encoding%" -d "%tagDir%" -sourcepath "%srcDir%" -cp "%lib%" ""%src%""
javadoc -locale "%locale%" -quiet -encoding "%encoding%" -charset "%encoding%" -d "%docDir%" -sourcepath "%srcDir%" -cp "%lib%" ""%src%""

jar -cfm "%outDir%\asarFs-%version%.jar" "%rootDir%\META_INF\MANIFEST.MF" -C "%tagDir%" "."
jar -cfm "%outDir%\asarFs-%version%-sources.jar" "%rootDir%\META_INF\MANIFEST.MF" -C "%srcDir%" "."
jar -cfm "%outDir%\asarFs-%version%-javadoc.jar" "%rootDir%\META_INF\MANIFEST.MF" -C "%docDir%" "."
