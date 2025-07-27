#!/bin/bash

# Simplified Build Script for Courier Service JDBC Project

echo "=== Building Courier Service JDBC Project (Simplified) ==="

# Create build directory
mkdir -p build
mkdir -p build/classes

# Compile Java files (without external dependencies for now)
echo "Compiling Java files..."
javac -d build/classes CourierServiceDB.java CourierServiceDemo.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create JAR file
    echo "Creating JAR file..."
    jar cfm courier-service-jdbc.jar manifest.txt -C build/classes .
    
    if [ $? -eq 0 ]; then
        echo "JAR file created successfully: courier-service-jdbc.jar"
        echo ""
        echo "=== Usage ==="
        echo "Note: This JAR contains the core classes but requires MySQL JDBC driver for database operations."
        echo ""
        echo "To use with database:"
        echo "1. Download MySQL JDBC driver:"
        echo "   wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar"
        echo ""
        echo "2. Run with driver:"
        echo "   java -cp courier-service-jdbc.jar:mysql-connector-java-8.0.33.jar CourierServiceDemo"
        echo ""
        echo "3. Or use in your application:"
        echo "   java -cp courier-service-jdbc.jar:mysql-connector-java-8.0.33.jar YourApplication"
    else
        echo "Error creating JAR file"
        exit 1
    fi
else
    echo "Compilation failed!"
    exit 1
fi 