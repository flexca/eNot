package io.github.flexca.enot.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShaUtils {

    private ShaUtils() {
    }

    public static byte[] sha1(byte[] input) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(input);
        return md.digest();
    }

    public static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input);
        byte[] digest = md.digest();
        return digest;
    }
}
