@echo off
@setlocal

cd /D %~dp0
set _UCASE=ABCDEFGHIJKLMNOPQRSTUVWXYZ
set _LCASE=abcdefghijklmnopqrstuvwxyz

set archetype_group=-DarchetypeGroupId=dev.simpleframework
set archetype_artifact=-DarchetypeArtifactId=simple-archetype-module
set archetype_version=-DarchetypeVersion=0.2-SNAPSHOT

set moduleName=%1
call :setModuleName
call :setPackageName
call :setSampleName

set module_group=-DgroupId=${groupId}
set module_artifact=-DartifactId=%moduleName%
set module_version=-Dversion=${version}
set module_package=-Dpackage=${package}.%packageName%
set module_sample=-Dsample=%sampleName%
set module_project=-Dproject=${artifactId}
set module_db=-Ddb=${db}

mvn archetype:generate %archetype_group% %archetype_artifact% %archetype_version% %module_group% %module_artifact% %module_version% %module_package% %module_sample% %module_project% %module_db%

:setModuleName
if "%moduleName%" == "" (
  set /p moduleName= "please input module name: "
)
if "%moduleName%" == "" (
  echo module name can not be empty
  pause
  exit /b 1
)
goto :eof


:setPackageName
set packageName=%moduleName%
set "check=%packageName%"
:loop1
if defined check (
  for /f "delims=- tokens=1*" %%x in ("%check%") do (
    set "packageName=%%x"
    set "check=%%y"
  )
  goto loop1
)

set "check=%packageName%"
:loop2
if defined check (
  for /f "delims=_ tokens=1*" %%x in ("%check%") do (
    set "packageName=%%x"
    set "check=%%y"
  )
  goto loop2
)

for %%x in (a b c d e f g h i j k l m n o p q r s t u v w x y z) do (
 call set packageName=%%packageName:%%x=%%x%%
)
goto :eof


:setSampleName
set "sampleName=%packageName%"
call set _prefix=%%sampleName:~0,1%%
call set _subfix=%%sampleName:~1%%
for %%x in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
  call set _prefix=%%_prefix:%%x=%%x%%
)
set "sampleName=%_prefix%%_subfix%"
goto :eof

@endlocal
