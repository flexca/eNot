package io.github.flexca.enot.core.serializer.context;

import io.github.flexca.enot.core.util.ShaUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public abstract class ContextNode {

    public abstract Object getValue();

    public String sha256Hex() throws NoSuchAlgorithmException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toBytes(out);
        byte[] sha256Value = ShaUtils.sha256(out.toByteArray());
        return HexFormat.of().formatHex(sha256Value);
    }

    protected abstract void toBytes(OutputStream out) throws IOException;
}
