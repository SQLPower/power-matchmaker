
rem SQL Power DQguru Repository Exporter Startup Script
rem Usage: sh repository-export.sh

echo "Starting SQL Power DQguru Repository Exporter"
echo "Using JRE Version:"
java -version

java -Xmx500m -XX:MaxPermSize=128M -jar repository-export.jar