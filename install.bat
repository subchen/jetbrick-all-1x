@echo off

set JAVA_HOME=C:\dev\jdk1.8.0
set JAVA_OPTS=-Xms256m -Xmx512m
set PATH=%JAVA_HOME%\bin;%PATH%

set ANT_HOME=C:\dev\apache-ant-1.8.4
set PATH=%ANT_HOME%\bin;%PATH%

:: gpg path
set PATH=C:\dev\git-1.8.1\bin;%PATH%

:: switch pwd
cd /d %~dp0

call ant -buildfile jetbrick-commons/build.xml install
call ant -buildfile jetbrick-ioc/build.xml install
call ant -buildfile jetbrick-webmvc/build.xml install
call ant -buildfile jetbrick-orm/build.xml install
call ant -buildfile jetbrick-all/build.xml install


pause &
