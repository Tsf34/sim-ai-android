#!/bin/sh
# Script Gradle — regénérez l'exécutable avec : gradle wrapper (ou Android Studio / IDE)
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec gradle "$@"
