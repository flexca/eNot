package io.github.flexca.enot.bertlv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceReaderTestUtils {

    private ResourceReaderTestUtils() {
    }

    public static byte[] readResourceFile(String path) throws IOException {

        try (InputStream is = ResourceReaderTestUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            return is.readAllBytes();
        }
    }

    public static String readResourceFileAsString(String path) throws IOException {

        byte[] binary = readResourceFile(path);
        return new String(binary, StandardCharsets.UTF_8);
    }
}
