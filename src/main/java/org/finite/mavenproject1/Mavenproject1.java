package org.finite.mavenproject1;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.finite.*;
import org.finite.ModuleManager.annotations.MNIClass;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class Mavenproject1 {

    @MNIClass("NetFunctions")
    public static class NetFunctions {
        // public static void 
        
        public static void CreateButton() {
            System.out.println("CreateButton called");
            // Implementation for creating a button
        }
    }

    public static void startServer(int port, String masmDirectory) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MASMHandler(masmDirectory)); // Changed context to root
        server.setExecutor(null); // Use default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class MASMHandler implements HttpHandler {
        private final String masmDirectory;
        public static OutputStream outputStream = new ByteArrayOutputStream();
        public static BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        public MASMHandler(String masmDirectory) {
            this.masmDirectory = masmDirectory;
            System.out.println("MASM Directory set to: " + masmDirectory);

            // Ensure outputStream is initialized
            if (!(outputStream instanceof ByteArrayOutputStream)) {
                outputStream = new ByteArrayOutputStream();
            }

            bufferedOutputStream = new BufferedOutputStream(outputStream);
            Functions.stdout = outputStream;
            Functions.bufferedStdout = bufferedOutputStream;

            System.out.println("Buffered Output Stream initialized.");
            System.out.println("Mavenproject1.MASMHandler initialized with MASM Directory: " + masmDirectory);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Request Method: " + exchange.getRequestMethod());
            System.out.println("Request Path: " + exchange.getRequestURI().getPath());

            // Clear the output stream before processing the request
            if (outputStream instanceof ByteArrayOutputStream) {
                ((ByteArrayOutputStream) outputStream).reset();
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String fileName = path.equals("/") ? "index.masm" : path.substring(path.lastIndexOf('/') + 1);
            System.out.println("Resolved File Name: " + fileName);

            Path filePath = Paths.get(masmDirectory, fileName);
            if (!Files.exists(filePath)) {
                System.out.println("File does not exist: " + filePath);
                exchange.sendResponseHeaders(404, -1); // Not Found
                return;
            }

            try {
                String masmCode = Files.readString(filePath);
                System.out.println("MASM Code Read Successfully");

                // Extract <script> blocks
                Pattern scriptPattern = Pattern.compile("<Jscript>(.*?)</Jscript>", Pattern.DOTALL);
                Matcher matcher = scriptPattern.matcher(masmCode);

                while (matcher.find()) {
                    String scriptCode = matcher.group(1);
                    System.out.println("Executing JavaScript: " + scriptCode);

                    try (Context context = Context.create()) {
                        JavaFunctions javaFunctions = new JavaFunctions();
                        context.getBindings("js").putMember("JavaFunctions", javaFunctions);
                        context.eval("js", scriptCode);
                    } catch (Exception e) {
                        System.out.println("Error executing JavaScript: " + e.getMessage());
                    }

                    // Remove the <script> block from the MASM code
                    masmCode = masmCode.replace(matcher.group(0), " ");
                }

                // Execute the MASM code and capture the output
                try {
                    org.finite.interp.executeMASMCode(masmCode);
                } catch (Exception e) {
                    System.out.println("Error executing MASM code: " + e.getMessage());
                    exchange.sendResponseHeaders(500, -1); // Internal Server Error
                    return;
                }

                // Flush the buffered output stream
                bufferedOutputStream.flush();

                // Retrieve the output from the ByteArrayOutputStream
                String responseContent = ((ByteArrayOutputStream) outputStream).toString("UTF-8");

                // Send the processed output as the response
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, responseContent.getBytes().length);
                exchange.getResponseBody().write(responseContent.getBytes());
            } catch (Exception e) {
                System.out.println("Exception occurred: " + e.getMessage());
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
            } finally {
                exchange.getResponseBody().close();
            }
        }
        
        public static class JavaFunctions {
            @HostAccess.Export
            public void log(String message) {
                System.out.println("Java Log: " + message);
            }
            @HostAccess.Export
            public String greet(String name) {
                return "Hello, " + name;
            }
            @HostAccess.Export
            public void appendToPage(String content) {
                try {
                    bufferedOutputStream.write(content.getBytes("UTF-8"));
                } catch (IOException e) {
                    System.out.println("Error appending content to page: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String inputFilePath = args.length > 0 ? args[0] : "./input.masm";
        File inputFile = new File(inputFilePath);
        String masmDirectory = new File(inputFile.getParent(), "masm_files").getAbsolutePath();
        startServer(port, masmDirectory);
    }
}
