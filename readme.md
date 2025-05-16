# NetProp

Welcome to NetProp, a Java runtime environment for Micro-Assembly on the web.

NetProp is a Java Program designed to run Micro-Assembly like PHP, rendering to HTML, CSS, and JS instead of the console.

## Features

- Execute Micro-Assembly (MASM) code and render output as web content.
- Built-in HTTP server to serve MASM files dynamically.
- Support for JavaScript execution within MASM files.
- Integration with Java for extended functionality using MNI (Micro-Assembly Native Interface).

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven build tool

### Setup

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd mavenproject1
   ```

2. Build the project using Maven:

   ```bash
   mvn clean package
   ```

3. Run the application:

   ```bash
   java -jar target/mavenproject1-1.0-SNAPSHOT-jar-with-dependencies.jar <path-to-input.masm>
   ```

### Directory Structure

- `src/main/java`: Contains the Java source code.
- `masm_files`: Directory for MASM files to be served by the application.
- `target`: Directory for compiled files and build artifacts.

## Usage

1. Place your MASM files in the `masm_files` directory.
2. Start the server and access it via `http://localhost:8080/`.
3. Use the browser to interact with the MASM-generated web pages.

## Example MASM File

```masm
lbl main
    DB $100 "<!doctype html> <head> <title>MASM Example</title></head>"
    out 1 $100
    DB $100 "<body> <h1>Hello, MASM!</h1> <p>This is a simple MASM example.</p></body>"
    out 1 $100
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.
