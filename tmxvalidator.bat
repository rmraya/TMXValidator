@echo off
pushd "%~dp0" 
bin\java.exe --module-path lib -m tmxvalidator/com.maxprograms.tmxvalidation.TMXValidator %* 