package com.github.flexca.enot.core.util;

import java.util.List;

public class BinaryUtils {

    private BinaryUtils() {
    }

    public static byte[] concatenateBinary(List<byte[]> input) {

        int totalLength = 0;
        for (byte[] array : input) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = new byte[totalLength];
        int currentPosition = 0;
        for (byte[] array : input) {
            if (array != null) {
                System.arraycopy(array, 0, result, currentPosition, array.length);
                currentPosition += array.length;
            }
        }

        return result;
    }
}
