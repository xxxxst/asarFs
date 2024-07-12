@echo off

set rootDir=%~dp0
set testDir=%rootDir%\test
set asarDir=%testDir%\app

if exist "%asarDir%" (
	asar p "%asarDir%\ccc" "%asarDir%\ccc.asar"
	asar p "%asarDir%" "%testDir%\app.asar"
)
