package com.example.evosuite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EvoSuiteRunner {
    public static String generateTests(String className, String projectCP) throws IOException {
        String cmd = String.format("java -jar /path/to/evosuite.jar -class %s -projectCP %s", className, projectCP);
        Process process = Runtime.getRuntime().exec(cmd);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            output.append(line).append("\n");
        }
        return output.toString();
    }
}