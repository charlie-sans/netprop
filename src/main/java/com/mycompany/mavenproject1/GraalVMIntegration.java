package com.mycompany.mavenproject1;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class GraalVMIntegration {
    public static void Cmain() {
        try (Context context = Context.create()) {
            // Example: Execute JavaScript code
            Value result = context.eval("js", "function add(a, b) { return a + b; } add(5, 3);");
            System.out.println("Result from JavaScript: " + result.asInt());

            // Example: Call Java from JavaScript
            context.getBindings("js").putMember("JavaGreet", (java.util.function.Function<String, String>) name -> "Hello, " + name);
            context.eval("js", "console.log(JavaGreet('Charlie'));");
        }
    }
}