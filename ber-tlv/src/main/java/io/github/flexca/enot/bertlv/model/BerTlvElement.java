package io.github.flexca.enot.bertlv.model;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract base class representing a BER-TLV (Basic Encoding Rules Tag-Length-Value) element
 * as defined by ITU-T X.690.
 * <p>
 * A TLV element consists of:
 * <ul>
 *   <li><b>Tag</b> — one to four bytes identifying the data object type</li>
 *   <li><b>Length</b> — the encoded byte count of the value field (definite or indefinite form)</li>
 *   <li><b>Value</b> — the content, which may be raw bytes ({@link BerTlvLeafElement})
 *       or a list of nested TLV elements ({@link BerTlvNodeElement})</li>
 * </ul>
 * <p>
 * Subclasses implement the composite pattern: leaf elements hold raw binary data,
 * while node elements hold a list of child {@code BerTlvElement} instances.
 *
 * @param <T> the value type — {@code byte[]} for leaf elements, {@code List<BerTlvElement<?>>} for node elements
 */
public abstract class BerTlvElement<T> {

    private byte[] tag;
    private boolean indefiniteForm = false;

    /**
     * Returns {@code true} if this is a primitive (leaf) element containing raw bytes,
     * or {@code false} if it is a constructed (node) element containing nested TLV children.
     *
     * @return {@code true} for leaf elements, {@code false} for node elements
     */
    public abstract boolean isLeaf();

    /**
     * Returns the value of this element.
     *
     * @return {@code byte[]} for leaf elements, {@code List<BerTlvElement<?>>} for node elements
     */
    public abstract T getValue();

    /**
     * Sets the value of this element.
     *
     * @param value the value to set
     */
    public abstract void setValue(T value);

    /**
     * Returns the total encoded byte length of this element, including tag bytes,
     * the length field, the value bytes, and the two-byte indefinite-form terminator ({@code 00 00})
     * if indefinite form is enabled.
     *
     * @return total encoded length in bytes
     */
    public abstract int getTotalLength();

    /**
     * Returns the byte length of the value field only, excluding tag and length bytes.
     * For node elements this is the sum of {@link #getTotalLength()} over all children.
     *
     * @return value length in bytes
     */
    public abstract int getValueLength();

    /**
     * Encodes this element in TLV format and writes the bytes to the given output stream.
     * For node elements, child elements are encoded recursively.
     *
     * @param out the output stream to write the encoded TLV bytes to
     * @throws IOException if an I/O error occurs writing to the stream
     */
    public abstract void encodeToStream(OutputStream out) throws IOException;

    /**
     * Returns the tag bytes of this element.
     *
     * @return tag bytes (1–4 bytes)
     */
    public byte[] getTag() {
        return tag;
    }

    /**
     * Sets the tag bytes of this element.
     *
     * @param tag tag bytes (1–4 bytes)
     */
    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    /**
     * Returns whether this element uses indefinite-form length encoding.
     * If {@code true}, the length field is encoded as {@code 0x80} and the value
     * is followed by a two-byte end-of-contents terminator ({@code 00 00}).
     *
     * @return {@code true} if indefinite-form encoding is used
     */
    public boolean isIndefiniteForm() {
        return indefiniteForm;
    }

    /**
     * Sets whether this element uses indefinite-form length encoding.
     *
     * @param indefiniteForm {@code true} to use indefinite-form encoding
     */
    public void setIndefiniteForm(boolean indefiniteForm) {
        this.indefiniteForm = indefiniteForm;
    }
}
