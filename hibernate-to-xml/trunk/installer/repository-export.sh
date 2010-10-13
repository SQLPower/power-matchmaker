#!/bin/sh

# SQL Power DQguru Repository Exporter Startup Script
# Usage: sh repository-export.sh

echo "Starting SQL Power DQguru Repository Exporter"
echo "Using JRE Version:"
java -version

java -Xmx1000m -XX:MaxPermSize=128m -jar repository-export.jar
