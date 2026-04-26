package io.github.flexca.enot.core.serializer.context;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ContextPrimitive extends ContextNode {

    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    protected void toBytes(OutputStream out) throws IOException {

        if(value == null) {
            out.write("null".getBytes(StandardCharsets.UTF_8));
            return;
        }

        if (value instanceof Boolean booleanValue) {
            out.write("bool:".getBytes(StandardCharsets.UTF_8));
            out.write(booleanValue ? 0x01 : 0x00);
        } else if (value instanceof String stringValue) {
            out.write("text:".getBytes(StandardCharsets.UTF_8));
            out.write(stringValue.getBytes(StandardCharsets.UTF_8));
        } else if (value instanceof Number numberValue) {
            out.write("number:".getBytes(StandardCharsets.UTF_8));
            if (numberValue instanceof Integer) {
                out.write(ByteBuffer.allocate(Integer.BYTES).putInt(numberValue.intValue()).array());
            } else if (numberValue instanceof Long) {
                out.write(ByteBuffer.allocate(Long.BYTES).putLong(numberValue.longValue()).array());
            } else if (numberValue instanceof Double) {
                out.write(ByteBuffer.allocate(Double.BYTES).putDouble(numberValue.doubleValue()).array());
            } else if (numberValue instanceof Float) {
                out.write(ByteBuffer.allocate(Float.BYTES).putFloat(numberValue.floatValue()).array());
            } else if (numberValue instanceof Short) {
                out.write(ByteBuffer.allocate(Short.BYTES).putShort(numberValue.shortValue()).array());
            } else if (numberValue instanceof Byte) {
                out.write(numberValue.byteValue());
            } else if (numberValue instanceof BigInteger bigInteger) {
                out.write(bigInteger.toByteArray());
            } else if (numberValue instanceof BigDecimal bigDecimal) {
                out.write(bigDecimal.stripTrailingZeros().toEngineeringString().getBytes(StandardCharsets.UTF_8));
            } else {
                out.write(numberValue.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
