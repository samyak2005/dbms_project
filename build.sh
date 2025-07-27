#!/bin/bash

# Courier Service JDBC Project Build Script

echo "=== Building Courier Service JDBC Project ==="

# Create build directory
mkdir -p build
mkdir -p build/classes

# Download MySQL JDBC driver if not present
if [ ! -f "mysql-connector-java-8.0.33.jar" ]; then
    echo "Downloading MySQL JDBC driver..."
    curl -L -o mysql-connector-java-8.0.33.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
fi

# Compile Java files
echo "Compiling Java files..."
javac -cp "mysql-connector-java-8.0.33.jar" -d build/classes CourierServiceDB.java CourierServiceDemo.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create JAR file
    echo "Creating JAR file..."
    jar cfm courier-service-jdbc.jar manifest.txt -C build/classes .
    
    if [ $? -eq 0 ]; then
        echo "JAR file created successfully: courier-service-jdbc.jar"
        echo ""
        echo "=== Usage ==="
        echo "Run the demo:"
        echo "java -cp courier-service-jdbc.jar:mysql-connector-java-8.0.33.jar CourierServiceDemo"
        echo ""
        echo "Or use in your application:"
        echo "java -cp courier-service-jdbc.jar:mysql-connector-java-8.0.33.jar YourApplication"
    else
        echo "Error creating JAR file"
        exit 1
    fi
else
    echo "Compilation failed!"
    exit 1
fi 