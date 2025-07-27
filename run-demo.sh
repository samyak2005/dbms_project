#!/bin/bash

# Run Courier Service Demo Script

echo "=== Running Courier Service JDBC Demo ==="

# Check if JAR file exists
if [ ! -f "courier-service-jdbc.jar" ]; then
    echo "JAR file not found. Building project first..."
    chmod +x build.sh
    ./build.sh
fi

# Check if MySQL driver exists
if [ ! -f "mysql-connector-java-8.0.33.jar" ]; then
    echo "MySQL JDBC driver not found. Downloading..."
    curl -L -o mysql-connector-java-8.0.33.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
fi

# Run the demo
echo "Starting demo..."
java -cp "courier-service-jdbc.jar:mysql-connector-java-8.0.33.jar" CourierServiceDemo

echo "Demo completed!" 