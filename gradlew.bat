@echo off
rem Script Gradle (Windows) — regénérez l'exécutable avec : gradle wrapper
set SCRIPT_DIR=%~dp0
gradle %*
