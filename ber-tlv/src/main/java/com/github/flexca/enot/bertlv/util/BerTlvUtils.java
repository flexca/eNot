package com.github.flexca.enot.bertlv.util;

import java.util.List;

public class BerTlvUtils {

    private BerTlvUtils() {
    }

    public static byte[] calculateLength(int valueSize, boolean indefiniteForm) {

        if (indefiniteForm) {
            return new byte[]{(byte) 0x80};
        }

        if (valueSize <= 127) {
            return new byte[]{(byte) valueSize};
        }

        int numBytes = 1;
        if (valueSize > 0xFFFFFF) {
            numBytes = 4;
        } else if (valueSize > 0xFFFF) {
            numBytes = 3;
        } else if (valueSize > 0xFF) {
            numBytes = 2;
        }

        byte[] lengthData = new byte[numBytes + 1];
        lengthData[0] = (byte) (0x80 | numBytes);
        for (int i = 0; i < numBytes; i++) {
            lengthData[numBytes - i] = (byte) ((valueSize >> (8 * i)) & 0xFF);
        }

        return lengthData;
    }

    public static boolean isValidTagLength(byte[] tagBinary) {

        if (tagBinary == null || tagBinary.length < 1 || tagBinary.length > 4) {
            return false;
        }

        if ((tagBinary[0] & 0x1F) != 0x1F) {
            return tagBinary.length == 1;
        }

        if (tagBinary.length == 1) return false;

        int tagValue = 0;
        for (int i = 1; i < tagBinary.length - 1; i++) {
            int b = tagBinary[i] & 0xFF;
            if ((b & 0x80) != 0x80) {
                return false;
            }
            tagValue = (tagValue << 7) | (b & 0x7F);
        }

        int lastByte = tagBinary[tagBinary.length - 1] & 0xFF;
        if ((lastByte & 0x80) == 0x80) {
            return false;
        }
        tagValue = (tagValue << 7) | (lastByte & 0x7F);

        return switch (tagBinary.length) {
            case 2 -> tagValue >= 31;
            case 3 -> tagValue >= 128;
            case 4 -> tagValue >= 16384;
            default -> false;
        };
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
