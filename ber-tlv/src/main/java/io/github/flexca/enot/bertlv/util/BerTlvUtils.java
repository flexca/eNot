package io.github.flexca.enot.bertlv.util;

import java.util.List;

/**
 * Low-level utility methods for BER-TLV encoding according to ITU-T X.690.
 * <p>
 * This class is not instantiable; all methods are static.
 */
public class BerTlvUtils {

    private BerTlvUtils() {
    }

    /**
     * Encodes a length value as BER length bytes according to ITU-T X.690 rules.
     * <ul>
     *   <li>Indefinite form: returns a single byte {@code 0x80}. The caller is responsible
     *       for writing the end-of-contents terminator ({@code 00 00}) after the value.</li>
     *   <li>Short definite form (0–127): returns a single byte equal to {@code valueSize}.</li>
     *   <li>Long definite form (128+): returns {@code 0x80 | n} followed by {@code n} big-endian
     *       bytes of {@code valueSize}, where {@code n} is the minimum number of bytes needed.</li>
     * </ul>
     *
     * @param valueSize    the byte length of the value field
     * @param indefiniteForm {@code true} to use indefinite-form encoding
     * @return the encoded length field as a byte array
     */
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

    /**
     * Validates the structure and value of a BER tag byte array according to ITU-T X.690.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>Must be 1–4 bytes in length.</li>
     *   <li>If the lower five bits of the first byte are not {@code 0x1F}: single-byte tag only.</li>
     *   <li>If the lower five bits are {@code 0x1F} (long-form indicator): each continuation byte
     *       except the last must have its high bit set ({@code 0x80}); the last byte must not.</li>
     *   <li>The encoded tag value must meet the minimum for its byte length:
     *       ≥ 31 for 2-byte tags, ≥ 128 for 3-byte tags, ≥ 16384 for 4-byte tags.</li>
     * </ul>
     *
     * @param tagBinary the tag bytes to validate
     * @return {@code true} if the tag is structurally valid, {@code false} otherwise
     */
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
}
