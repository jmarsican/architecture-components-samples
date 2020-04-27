package com.javiermarsicano.githubsample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestResources {
    public static Path apiDemosApk() {
        return resourcePathToLocalPath("apps/app-mock.apk");
    }

    public static Path resourcePathToLocalPath(String resourcePath) {
        URL url = ClassLoader.getSystemResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException(String.format("Cannot find the '%s' resource", resourcePath));
        }
        return Paths.get(url.getPath());
    }

    private static File getFileFromResources(String fileName) {
        URL resource = ClassLoader.getSystemResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Cannot find the '%s' resource", fileName));
        } else {
            return new File(resource.getFile());
        }

    }

    public static String loadJsonResponse(String fileName) {
        try {
            return loadFile(getFileFromResources("json/responses/" + fileName + ".json"));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Cannot find the '%s' resource", fileName));
        }
    }

    private static String loadFile(File file) throws IOException {

        if (file == null) return null;

        try (FileReader reader = new FileReader(file);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
