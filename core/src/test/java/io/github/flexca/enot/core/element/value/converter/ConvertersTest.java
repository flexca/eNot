package io.github.flexca.enot.core.element.value.converter;

import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConvertersTest {

    // -----------------------------------------------------------------------
    // BooleanToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testBooleanTrueConvertsToOneByte() {
        assertThat(new BooleanToBinaryConverter().toBinary(true))
                .isEqualTo(new byte[]{0x01});
    }

    @Test
    void testBooleanFalseConvertsToZeroByte() {
        assertThat(new BooleanToBinaryConverter().toBinary(false))
                .isEqualTo(new byte[]{0x00});
    }

    @Test
    void testBooleanNullReturnsNull() {
        assertThat(new BooleanToBinaryConverter().toBinary(null)).isNull();
    }

    @Test
    void testBooleanWrongTypeThrows() {
        assertThatThrownBy(() -> new BooleanToBinaryConverter().toBinary("true"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // BinaryToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testBinaryPassthroughReturnsSameArray() {
        byte[] input = {0x01, 0x02, 0x03};
        assertThat(new BinaryToBinaryConverter().toBinary(input)).isSameAs(input);
    }

    @Test
    void testBinaryNullReturnsNull() {
        assertThat(new BinaryToBinaryConverter().toBinary(null)).isNull();
    }

    @Test
    void testBinaryWrongTypeThrows() {
        assertThatThrownBy(() -> new BinaryToBinaryConverter().toBinary("bytes"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // TextToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testTextConvertsToUtf8Bytes() {
        String input = "hello";
        assertThat(new TextToBinaryConverter().toBinary(input))
                .isEqualTo(input.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testTextUtf8MultibyteEncoding() {
        // "café" contains a 2-byte UTF-8 sequence for é (U+00E9)
        String input = "café";
        assertThat(new TextToBinaryConverter().toBinary(input))
                .isEqualTo(input.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testTextEmptyStringConvertsToEmptyArray() {
        assertThat(new TextToBinaryConverter().toBinary(""))
                .isEqualTo(new byte[0]);
    }

    @Test
    void testTextNullReturnsNull() {
        assertThat(new TextToBinaryConverter().toBinary(null)).isNull();
    }

    @Test
    void testTextWrongTypeThrows() {
        assertThatThrownBy(() -> new TextToBinaryConverter().toBinary(42))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // IntegerToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testIntegerPositiveConverts() {
        assertThat(new IntegerToBinaryConverter().toBinary(1))
                .isEqualTo(BigInteger.valueOf(1).toByteArray());
    }

    @Test
    void testIntegerZeroConverts() {
        assertThat(new IntegerToBinaryConverter().toBinary(0))
                .isEqualTo(BigInteger.ZERO.toByteArray());
    }

    @Test
    void testIntegerNegativeConverts() {
        assertThat(new IntegerToBinaryConverter().toBinary(-1))
                .isEqualTo(BigInteger.valueOf(-1).toByteArray());
    }

    @Test
    void testLongConverts() {
        long value = Long.MAX_VALUE;
        assertThat(new IntegerToBinaryConverter().toBinary(value))
                .isEqualTo(BigInteger.valueOf(value).toByteArray());
    }

    @Test
    void testBigIntegerConverts() {
        BigInteger value = new BigInteger("123456789012345678901234567890");
        assertThat(new IntegerToBinaryConverter().toBinary(value))
                .isEqualTo(value.toByteArray());
    }

    @Test
    void testIntegerNullReturnsNull() {
        assertThat(new IntegerToBinaryConverter().toBinary(null)).isNull();
    }

    @Test
    void testIntegerWrongTypeThrows() {
        assertThatThrownBy(() -> new IntegerToBinaryConverter().toBinary("123"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // EmptyToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testEmptyConverterAlwaysReturnsNull() {
        EmptyToBinaryConverter converter = new EmptyToBinaryConverter();
        assertThat(converter.toBinary(null)).isNull();
        assertThat(converter.toBinary("anything")).isNull();
        assertThat(converter.toBinary(42)).isNull();
    }

    // -----------------------------------------------------------------------
    // UnsupportedToBinaryConverter
    // -----------------------------------------------------------------------

    @Test
    void testUnsupportedConverterAlwaysThrows() {
        UnsupportedToBinaryConverter converter = new UnsupportedToBinaryConverter();
        assertThatThrownBy(() -> converter.toBinary(null))
                .isInstanceOf(EnotInvalidArgumentException.class);
        assertThatThrownBy(() -> converter.toBinary("value"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }
}
