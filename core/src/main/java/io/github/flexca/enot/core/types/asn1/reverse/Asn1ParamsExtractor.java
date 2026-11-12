package io.github.flexca.enot.core.types.asn1.reverse;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.types.system.SystemKind;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import io.github.flexca.enot.core.types.system.attribute.BitOrder;
import io.github.flexca.enot.core.types.system.attribute.ByteOrder;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.DateTimeUtils;
import io.github.flexca.enot.core.util.PlaceholderUtils;
import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1VisibleString;
import org.bouncycastle.asn1.BERTags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extracts placeholder parameter values from an ASN.1 binary structure by matching it
 * against an eNot template.
 *
 * <p>This is the inverse of serialization: given a template and an actual binary (for example a
 * certificate request produced by a third party), the extractor looks for the best match of every
 * template element in the binary and collects the concrete values found at placeholder positions.
 * The result is a {@code Map<String, Object>} that can be passed directly to
 * {@link SerializationContext.Builder#withParams(Map)}.</p>
 *
 * <h2>Matching model</h2>
 * The template is the source of truth: for each template element the extractor searches the not yet
 * consumed binary elements of the current level and picks the best candidate. Binary elements that
 * are not described by the template are ignored, template elements that cannot be found in the
 * binary produce no params and never cause a failure. The order of binary elements does not matter,
 * for SEQUENCE and SET alike. When several candidates fit equally well, the first one at or after the
 * last consumed position wins, so binaries that follow the template order are matched in that order.
 *
 * <p>A candidate is accepted when:</p>
 * <ul>
 *   <li>its ASN.1 type fits the element (string types are interchangeable, because requesters often
 *       use another string type than the template, e.g. PRINTABLE_STRING instead of UTF8_STRING);</li>
 *   <li>for TAGGED_OBJECT the context tag number matches, implicit and explicit tagging are both supported;</li>
 *   <li>literal (non-placeholder) bodies are equal to the binary value, e.g. an OID which identifies
 *       the RDN or the extension; a differing literal makes the candidate unacceptable;</li>
 *   <li>required literals of nested elements are present in the candidate.</li>
 * </ul>
 * Among acceptable candidates the one that matches the most template content is chosen.
 *
 * <h2>Supported template constructs</h2>
 * <ul>
 *   <li><b>ASN.1 containers</b> (SEQUENCE, SET) — children are searched in the container.</li>
 *   <li><b>ASN.1 leaf types</b> (strings, OID, INTEGER, BOOLEAN, times, BIT_STRING, OCTET_STRING)
 *       — the concrete binary value is stored under the placeholder name.</li>
 *   <li><b>TAGGED_OBJECT</b> — the tagged wrapper is unwrapped and the body is matched recursively.</li>
 *   <li><b>OCTET_STRING with nested ASN.1 body</b> — the octets are parsed as ASN.1 and the body
 *       is matched recursively; a placeholder body receives the hex value.</li>
 *   <li><b>Binary transformations</b> consumed by BIT_STRING and OCTET_STRING are reversed:
 *       {@code bit_map} (every placeholder receives a boolean, the bit at its position),
 *       {@code hex_to_bin} / {@code base64_to_bin} (hex / base64 text), {@code ip_address_to_bytes}
 *       (textual IPv4 / IPv6 address), including nesting such as {@code hex_to_bin} over {@code bin_to_hex}.
 *       {@code sha1} is not reversible and yields nothing. Placeholders of system and global variables
 *       ({@code ${system.*}}, {@code ${global.*}}) are never extracted.</li>
 *   <li><b>LOOP</b> — every binary element that matches the loop body (wherever it is located) becomes
 *       an item of the {@code List} stored under the {@code items_name} key.</li>
 *   <li><b>GROUP</b> — body elements are matched at the same level and results are stored in a nested
 *       map under the {@code group_name} key, the key is absent when nothing matched.</li>
 *   <li><b>CONDITION</b>, <b>REFERENCE</b> and custom element types with resolved element body
 *       (e.g. dynamic groups) — transparent, the body is matched at the same level.</li>
 * </ul>
 *
 * <h2>Literal (non-placeholder) bodies</h2>
 * Elements whose body is a hard-coded literal (e.g. a fixed OID) consume one binary element
 * but do not produce any param entry; their value is only used to choose the right candidate.
 *
 * <p>Instances are stateless and thread-safe.</p>
 */
public class Asn1ParamsExtractor {

    private enum Outcome {
        MATCHED,
        ABSENT,
        // a leaf with literal body was found by type, but only with different values
        CONFLICT
    }

    private enum Status {
        OK,
        INCOMPATIBLE,
        MISMATCH
    }

    private record Eval(Status status, int score, Map<String, Object> params) {

        static final Eval INCOMPATIBLE = new Eval(Status.INCOMPATIBLE, 0, Collections.emptyMap());
        static final Eval MISMATCH = new Eval(Status.MISMATCH, 0, Collections.emptyMap());

        static Eval ok(int score, Map<String, Object> params) {
            return new Eval(Status.OK, score, params);
        }
    }

    /**
     * Binary elements of one level together with the bookkeeping of already consumed ones.
     */
    private static final class Frame {

        private final List<ASN1Encodable> items;
        private final BitSet used;
        private int cursor;
        private int score;

        Frame(List<ASN1Encodable> items) {
            // elements built in memory (e.g. RDN, Extension) are not ASN1Primitive instances
            this.items = items.stream().map(item -> (ASN1Encodable) item.toASN1Primitive()).toList();
            this.used = new BitSet(this.items.size());
        }

        private Frame(Frame other) {
            this.items = other.items;
            this.used = (BitSet) other.used.clone();
            this.cursor = other.cursor;
            this.score = other.score;
        }

        Frame copy() {
            return new Frame(this);
        }

        void adopt(Frame other) {
            used.clear();
            used.or(other.used);
            cursor = other.cursor;
            score = other.score;
        }

        boolean consumedSameAs(Frame other) {
            return used.equals(other.used);
        }
    }

    /**
     * Decodes {@code base64Asn1} to DER bytes and delegates to
     * {@link #extractParams(List, byte[])}.
     *
     * @param template    parsed eNot template elements
     * @param base64Asn1  standard Base64-encoded DER binary
     * @return map of placeholder name to extracted value
     * @throws EnotInvalidArgumentException if Base64 decoding or ASN.1 parsing fails
     */
    public Map<String, Object> extractParams(List<EnotElement> template, String base64Asn1) {
        byte[] binary;
        try {
            binary = Base64.getDecoder().decode(base64Asn1);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during base64 decoding: " + e.getMessage(), e);
        }
        return extractParams(template, binary);
    }

    /**
     * Parses the raw DER binary and matches it against {@code template}, returning
     * all extracted placeholder values.
     *
     * @param template    parsed eNot template elements
     * @param asn1Binary  raw DER-encoded binary
     * @return map of placeholder name to extracted value
     * @throws EnotInvalidArgumentException if ASN.1 parsing fails
     */
    public Map<String, Object> extractParams(List<EnotElement> template, byte[] asn1Binary) {
        ASN1Encodable root;
        try {
            root = ASN1Primitive.fromByteArray(asn1Binary);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("failure during ASN.1 parsing: " + e.getMessage(), e);
        }
        return extractParams(template, Collections.singletonList(root));
    }

    /**
     * Matches already parsed ASN.1 elements (siblings located at the same level, for example RDNs of a
     * subject or extensions of a certificate request) against {@code template}, returning all extracted
     * placeholder values. Template elements are searched in {@code asn1Elements} regardless of their order,
     * elements that are not described by the template are ignored.
     *
     * @param template      parsed eNot template elements
     * @param asn1Elements  ASN.1 elements to extract params from
     * @return map of placeholder name to extracted value
     */
    public Map<String, Object> extractParams(List<EnotElement> template, List<ASN1Encodable> asn1Elements) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (template == null || asn1Elements == null) {
            return result;
        }
        Frame frame = new Frame(asn1Elements);
        for (EnotElement element : template) {
            matchElement(element, frame, result);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Core matching logic
    // -------------------------------------------------------------------------

    private Outcome matchElement(EnotElement element, Frame frame, Map<String, Object> dest) {
        if (Asn1TypeSpecification.TYPE_NAME.equals(element.getType())) {
            return matchAsn1Element(element, frame, dest);
        } else if (SystemTypeSpecification.TYPE_NAME.equals(element.getType())) {
            return matchSystemElement(element, frame, dest);
        }
        // custom element types (e.g. dynamic groups) which resolved their body into elements are transparent
        return matchTransparentElement(element, frame, dest, null);
    }

    /**
     * Searches the unconsumed binary elements for the best candidate of an ASN.1 template element
     * and, when found, consumes it and stores the extracted params.
     */
    private Outcome matchAsn1Element(EnotElement element, Frame frame, Map<String, Object> dest) {
        Asn1Tag tag = getTag(element);
        if (tag == null) {
            return Outcome.ABSENT;
        }

        Eval best = null;
        int bestIndex = -1;
        boolean mismatch = false;
        int size = frame.items.size();
        for (int step = 0; step < size; step++) {
            int index = (frame.cursor + step) % size;
            if (frame.used.get(index)) {
                continue;
            }
            Eval eval = evaluate(element, tag, frame.items.get(index));
            if (eval.status() == Status.MISMATCH) {
                mismatch = true;
            } else if (eval.status() == Status.OK && (best == null || eval.score() > best.score())) {
                best = eval;
                bestIndex = index;
            }
        }

        if (best == null) {
            return mismatch ? Outcome.CONFLICT : Outcome.ABSENT;
        }
        frame.used.set(bestIndex);
        frame.cursor = bestIndex + 1;
        frame.score += best.score();
        dest.putAll(best.params());
        return Outcome.MATCHED;
    }

    /**
     * Checks whether {@code candidate} can be a binary representation of {@code element}, without consuming anything.
     */
    private Eval evaluate(EnotElement element, Asn1Tag tag, ASN1Encodable candidate) {
        return switch (tag) {
            case SEQUENCE -> candidate instanceof ASN1Sequence sequence
                    ? evaluateChildren(element.getBody(), Arrays.asList(sequence.toArray()))
                    : Eval.INCOMPATIBLE;
            case SET -> candidate instanceof ASN1Set set
                    ? evaluateChildren(element.getBody(), Arrays.asList(set.toArray()))
                    : Eval.INCOMPATIBLE;
            case TAGGED_OBJECT -> evaluateTaggedObject(element, candidate);
            case OCTET_STRING -> evaluateOctetString(element, candidate);
            default -> evaluateLeaf(element, tag, candidate);
        };
    }

    private Eval evaluateChildren(Object body, List<ASN1Encodable> children) {
        Frame frame = new Frame(children);
        Map<String, Object> params = new LinkedHashMap<>();
        if (!matchBodyStrictly(body, frame, params)) {
            return Eval.INCOMPATIBLE;
        }
        return Eval.ok(1 + frame.score, params);
    }

    private Eval evaluateTaggedObject(EnotElement element, ASN1Encodable candidate) {
        if (!(candidate instanceof ASN1TaggedObject taggedObject)) {
            return Eval.INCOMPATIBLE;
        }
        Integer explicit = getIntegerAttribute(element, Asn1Attribute.EXPLICIT);
        Integer implicit = getIntegerAttribute(element, Asn1Attribute.IMPLICIT);
        Integer tagNumber = explicit != null ? explicit : implicit;
        if (tagNumber == null || !taggedObject.hasContextTag(tagNumber)) {
            return Eval.INCOMPATIBLE;
        }

        ASN1Encodable inner;
        try {
            inner = explicit != null
                    ? taggedObject.getExplicitBaseObject().toASN1Primitive()
                    : unwrapImplicit(taggedObject, element.getBody());
        } catch (RuntimeException e) {
            return Eval.INCOMPATIBLE;
        }
        if (inner == null) {
            return Eval.INCOMPATIBLE;
        }
        return evaluateChildren(element.getBody(), Collections.singletonList(inner));
    }

    private ASN1Encodable unwrapImplicit(ASN1TaggedObject taggedObject, Object body) {
        Integer universalTag = body instanceof EnotElement bodyElement ? getUniversalTagNumber(bodyElement) : null;
        if (universalTag == null) {
            return null;
        }
        return taggedObject.getBaseUniversal(false, universalTag);
    }

    private Eval evaluateOctetString(EnotElement element, ASN1Encodable candidate) {
        if (!(candidate instanceof ASN1OctetString octetString)) {
            return Eval.INCOMPATIBLE;
        }
        Object body = element.getBody();
        if (PlaceholderUtils.isPlaceholder(body)) {
            Map<String, Object> params = new LinkedHashMap<>();
            assignPlaceholder(body, HexFormat.of().formatHex(octetString.getOctets()), params);
            return Eval.ok(2, params);
        }
        if (body instanceof EnotElement bodyElement && isTransform(bodyElement)) {
            return evaluateTransform(bodyElement, octetString.getOctets());
        }
        if (body instanceof EnotElement || body instanceof List<?>) {
            List<ASN1Encodable> inner = parseAll(octetString.getOctets());
            if (inner == null) {
                return Eval.INCOMPATIBLE;
            }
            return evaluateChildren(body, inner);
        }
        return Eval.ok(1, Collections.emptyMap());
    }

    private Eval evaluateTransform(EnotElement transform, byte[] data) {
        Map<String, Object> params = new LinkedHashMap<>();
        return decodeTransform(transform, data, params) ? Eval.ok(2, params) : Eval.INCOMPATIBLE;
    }

    private Eval evaluateLeaf(EnotElement element, Asn1Tag tag, ASN1Encodable candidate) {
        int fit = tagFit(tag, candidate);
        if (fit == 0) {
            return Eval.INCOMPATIBLE;
        }
        Object body = element.getBody();
        if (PlaceholderUtils.isPlaceholder(body)) {
            Map<String, Object> params = new LinkedHashMap<>();
            assignPlaceholder(body, extractLeafValue(candidate), params);
            return Eval.ok(fit, params);
        }
        if (body instanceof EnotElement transform && isTransform(transform) && candidate instanceof ASN1BitString bitString) {
            return evaluateTransform(transform, bitString.getBytes());
        }
        if (isLiteral(body)) {
            Optional<String> actual = comparableValue(candidate);
            if (actual.isPresent() && !literalEquals(candidate, actual.get(), String.valueOf(body))) {
                return Eval.MISMATCH;
            }
            return Eval.ok(fit + 2, Collections.emptyMap());
        }
        return Eval.ok(fit, Collections.emptyMap());
    }

    private boolean literalEquals(ASN1Encodable candidate, String actual, String literal) {
        return candidate instanceof ASN1Boolean ? actual.equalsIgnoreCase(literal) : actual.equals(literal);
    }

    /**
     * Matches template body content (a single element or a list) against the binary level.
     * Returns {@code false} when the binary contradicts the template: a literal differs or a required
     * literal is missing, which means that the binary element described by the body is something else.
     * Missing non-literal elements are not an error.
     */
    private boolean matchBodyStrictly(Object templateBody, Frame frame, Map<String, Object> dest) {
        for (EnotElement element : elementsOf(templateBody)) {
            Outcome outcome = matchElement(element, frame, dest);
            if (outcome == Outcome.CONFLICT) {
                return false;
            }
            if (outcome == Outcome.ABSENT && !element.isOptional() && requiresLiteral(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} when the element can only be present if some of its mandatory literals
     * (e.g. an OID) are present, so its absence identifies a different binary structure.
     */
    private boolean requiresLiteral(EnotElement element) {
        Object body = element.getBody();
        if (isLiteral(body)) {
            return Asn1TypeSpecification.TYPE_NAME.equals(element.getType());
        }
        if (SystemTypeSpecification.TYPE_NAME.equals(element.getType())) {
            SystemKind kind = getKind(element);
            if (kind != SystemKind.GROUP && kind != SystemKind.REFERENCE) {
                return false;
            }
        }
        for (EnotElement child : elementsOf(body)) {
            if (!child.isOptional() && requiresLiteral(child)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<EnotElement> elementsOf(Object body) {
        if (body instanceof EnotElement element) {
            return Collections.singletonList(element);
        }
        if (body instanceof List<?> listBody) {
            return (List<EnotElement>) (List<?>) listBody.stream()
                    .filter(e -> e instanceof EnotElement)
                    .toList();
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // System element handlers
    // -------------------------------------------------------------------------

    private Outcome matchSystemElement(EnotElement element, Frame frame, Map<String, Object> dest) {
        SystemKind kind = getKind(element);
        if (kind == null) {
            return Outcome.ABSENT;
        }
        return switch (kind) {
            case LOOP -> matchLoopElement(element, frame, dest);
            case GROUP -> matchTransparentElement(element, frame, dest, getRequiredName(element, SystemAttribute.GROUP_NAME));
            case CONDITION, REFERENCE -> matchTransparentElement(element, frame, dest, null);
            default -> Outcome.ABSENT;
        };
    }

    /**
     * Matches a LOOP element: repeatedly looks for binary elements matching the loop body, wherever they are
     * located among the unconsumed ones. Results are stored as a {@code List<Map<String, Object>>}
     * under the loop's {@code items_name} key.
     */
    private Outcome matchLoopElement(EnotElement loopElement, Frame frame, Map<String, Object> dest) {
        String itemsName = getRequiredName(loopElement, SystemAttribute.ITEMS_NAME);

        List<Object> items = new ArrayList<>();
        while (true) {
            Frame trial = frame.copy();
            Map<String, Object> iteration = new LinkedHashMap<>();
            if (!matchBodyStrictly(loopElement.getBody(), trial, iteration) || trial.consumedSameAs(frame)) {
                break;
            }
            items.add(iteration);
            frame.adopt(trial);
        }

        dest.put(itemsName, items);
        return items.isEmpty() ? Outcome.ABSENT : Outcome.MATCHED;
    }

    /**
     * Matches the body of an element that does not consume binary on its own (GROUP, CONDITION, REFERENCE,
     * custom types). Nothing is written when the body does not match or consumes nothing. When
     * {@code resultName} is set the results are stored in a nested map under that name, otherwise they
     * are merged into {@code dest}.
     */
    private Outcome matchTransparentElement(EnotElement element, Frame frame, Map<String, Object> dest, String resultName) {
        Object body = element.getBody();
        if (!(body instanceof EnotElement) && !(body instanceof List<?>)) {
            return Outcome.ABSENT;
        }
        Frame trial = frame.copy();
        Map<String, Object> result = new LinkedHashMap<>();
        if (!matchBodyStrictly(body, trial, result) || trial.consumedSameAs(frame)) {
            return Outcome.ABSENT;
        }
        frame.adopt(trial);
        if (resultName != null) {
            dest.put(resultName, result);
        } else {
            dest.putAll(result);
        }
        return Outcome.MATCHED;
    }

    private String getRequiredName(EnotElement element, SystemAttribute attribute) {
        Object name = element.getAttribute(attribute);
        if (!(name instanceof String nameValue)) {
            throw new EnotInvalidArgumentException(element.getAttribute(SystemAttribute.KIND)
                    + " element is missing the " + attribute.getName() + " attribute");
        }
        return nameValue;
    }

    // -------------------------------------------------------------------------
    // Type checks and value extraction
    // -------------------------------------------------------------------------

    /**
     * Returns how well the concrete ASN.1 class of {@code encodable} fits {@code expected}:
     * {@code 2} exact, {@code 1} compatible (another string type), {@code 0} not compatible.
     */
    private int tagFit(Asn1Tag expected, ASN1Encodable encodable) {
        boolean exact = switch (expected) {
            case SEQUENCE -> encodable instanceof ASN1Sequence;
            case SET -> encodable instanceof ASN1Set;
            case OBJECT_IDENTIFIER -> encodable instanceof ASN1ObjectIdentifier;
            case UTF8_STRING -> encodable instanceof ASN1UTF8String;
            case PRINTABLE_STRING -> encodable instanceof ASN1PrintableString;
            case IA5_STRING -> encodable instanceof ASN1IA5String;
            case VISIBLE_STRING -> encodable instanceof ASN1VisibleString;
            case BMP_STRING -> encodable instanceof ASN1BMPString;
            case BOOLEAN -> encodable instanceof ASN1Boolean;
            case INTEGER -> encodable instanceof ASN1Integer;
            case NULL -> encodable instanceof ASN1Null;
            case OCTET_STRING -> encodable instanceof ASN1OctetString;
            case BIT_STRING -> encodable instanceof ASN1BitString;
            case UTC_TIME -> encodable instanceof ASN1UTCTime;
            case GENERALIZED_TIME -> encodable instanceof ASN1GeneralizedTime;
            case TAGGED_OBJECT -> encodable instanceof ASN1TaggedObject;
        };
        if (exact) {
            return 2;
        }
        return isStringTag(expected) && isCharacterString(encodable) ? 1 : 0;
    }

    // BIT STRING implements ASN1String in BouncyCastle, but it is not a character string
    private boolean isCharacterString(ASN1Encodable encodable) {
        return encodable instanceof ASN1String && !(encodable instanceof ASN1BitString);
    }

    private boolean isStringTag(Asn1Tag tag) {
        return switch (tag) {
            case UTF8_STRING, PRINTABLE_STRING, IA5_STRING, VISIBLE_STRING, BMP_STRING -> true;
            default -> false;
        };
    }

    private Integer getUniversalTagNumber(EnotElement element) {
        if (!Asn1TypeSpecification.TYPE_NAME.equals(element.getType())) {
            return null;
        }
        Asn1Tag tag = getTag(element);
        if (tag == null) {
            return null;
        }
        return switch (tag) {
            case SEQUENCE -> BERTags.SEQUENCE;
            case SET -> BERTags.SET;
            case OBJECT_IDENTIFIER -> BERTags.OBJECT_IDENTIFIER;
            case UTF8_STRING -> BERTags.UTF8_STRING;
            case PRINTABLE_STRING -> BERTags.PRINTABLE_STRING;
            case IA5_STRING -> BERTags.IA5_STRING;
            case VISIBLE_STRING -> BERTags.VISIBLE_STRING;
            case BMP_STRING -> BERTags.BMP_STRING;
            case BOOLEAN -> BERTags.BOOLEAN;
            case INTEGER -> BERTags.INTEGER;
            case NULL -> BERTags.NULL;
            case OCTET_STRING -> BERTags.OCTET_STRING;
            case BIT_STRING -> BERTags.BIT_STRING;
            case UTC_TIME -> BERTags.UTC_TIME;
            case GENERALIZED_TIME -> BERTags.GENERALIZED_TIME;
            case TAGGED_OBJECT -> null;
        };
    }

    private boolean isLiteral(Object body) {
        if (body == null || body instanceof EnotElement || body instanceof List<?>) {
            return false;
        }
        return !PlaceholderUtils.isPlaceholder(body);
    }

    /**
     * Value of the binary element in the form comparable with a literal template body,
     * empty when the type does not support comparison.
     */
    private Optional<String> comparableValue(ASN1Encodable encodable) {
        if (encodable instanceof ASN1ObjectIdentifier oid) return Optional.of(oid.getId());
        if (encodable instanceof ASN1Boolean bool) return Optional.of(String.valueOf(bool.isTrue()));
        if (encodable instanceof ASN1Integer integer) return Optional.of(integer.getValue().toString(10));
        if (isCharacterString(encodable)) return Optional.of(((ASN1String) encodable).getString());
        return Optional.empty();
    }

    private List<ASN1Encodable> parseAll(byte[] binary) {
        List<ASN1Encodable> result = new ArrayList<>();
        try (ASN1InputStream stream = new ASN1InputStream(binary)) {
            ASN1Primitive primitive;
            while ((primitive = stream.readObject()) != null) {
                result.add(primitive);
            }
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    /**
     * Extracts the Java value from a leaf ASN.1 element. String types return a {@code String},
     * INTEGER returns a decimal {@code String}, BOOLEAN returns a {@code Boolean},
     * BIT_STRING and OCTET_STRING fall through to hex-encoded strings, date/time types return
     * a formatted {@code String}, NULL returns {@code null}.
     */
    private Object extractLeafValue(ASN1Encodable encodable) {
        if (encodable instanceof ASN1ObjectIdentifier oid) return oid.getId();
        if (encodable instanceof ASN1Boolean bool) return bool.isTrue();
        if (encodable instanceof ASN1Integer integer) return integer.getValue().toString(10);
        if (isCharacterString(encodable)) return ((ASN1String) encodable).getString();
        if (encodable instanceof ASN1BitString bs) return HexFormat.of().formatHex(bs.getBytes());
        if (encodable instanceof ASN1OctetString os) return HexFormat.of().formatHex(os.getOctets());
        if (encodable instanceof ASN1GeneralizedTime gt) {
            try {
                Date date = gt.getDate();
                return DateTimeUtils.format(DateTimeUtils.toZonedDateTime(date));
            } catch (Exception e) {
                return null;
            }
        }
        if (encodable instanceof ASN1UTCTime ut) {
            try {
                Date date = ut.getDate();
                return DateTimeUtils.format(DateTimeUtils.toZonedDateTime(date));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * If {@code body} is a placeholder expression, extracts the variable name and stores
     * {@code value} in {@code dest}. Absent (null) values and system/global variables
     * (e.g. {@code system.subject_key_identifier}, calculated during serialization) are not stored.
     */
    private void assignPlaceholder(Object body, Object value, Map<String, Object> dest) {
        if (value == null) {
            return;
        }
        PlaceholderUtils.extractPlaceholder(body)
                .filter(name -> !PlaceholderUtils.isSystemVariable(name) && !PlaceholderUtils.isGlobalVariable(name))
                .ifPresent(name -> dest.put(name, value));
    }

    // -------------------------------------------------------------------------
    // Reverse of system transformations (bit_map, hex_to_bin, ip_address_to_bytes, ...)
    // -------------------------------------------------------------------------

    private boolean isTransform(EnotElement element) {
        if (!SystemTypeSpecification.TYPE_NAME.equals(element.getType())) {
            return false;
        }
        SystemKind kind = getKind(element);
        return kind != null && switch (kind) {
            case BIT_MAP, SHA1, HEX_TO_BIN, BIN_TO_HEX, BASE64_TO_BIN, BIN_TO_BASE64, IP_ADDRESS_TO_BYTES -> true;
            default -> false;
        };
    }

    /**
     * Reverses a transformation that produced binary (the element consumed by a BIT_STRING or OCTET_STRING).
     * Returns {@code false} when the binary cannot be a result of the transformation.
     */
    private boolean decodeTransform(EnotElement transform, byte[] data, Map<String, Object> dest) {
        SystemKind kind = getKind(transform);
        if (kind == null) {
            return true;
        }
        return switch (kind) {
            case BIT_MAP -> decodeBitMap(transform, data, dest);
            case HEX_TO_BIN -> decodeTextBody(transform.getBody(), HexFormat.of().formatHex(data), dest);
            case BASE64_TO_BIN -> decodeTextBody(transform.getBody(), Base64.getEncoder().encodeToString(data), dest);
            case IP_ADDRESS_TO_BYTES -> {
                String address = toIpAddress(data);
                yield address != null && decodeTextBody(transform.getBody(), address, dest);
            }
            // sha1 is not reversible, nothing to extract
            default -> true;
        };
    }

    private boolean decodeBinaryBody(Object body, byte[] data, Map<String, Object> dest) {
        if (PlaceholderUtils.isPlaceholder(body)) {
            assignPlaceholder(body, HexFormat.of().formatHex(data), dest);
            return true;
        }
        if (body instanceof EnotElement transform && isTransform(transform)) {
            return decodeTransform(transform, data, dest);
        }
        return true;
    }

    private boolean decodeTextBody(Object body, String text, Map<String, Object> dest) {
        if (PlaceholderUtils.isPlaceholder(body)) {
            assignPlaceholder(body, text, dest);
            return true;
        }
        if (body instanceof EnotElement transform && isTransform(transform)) {
            SystemKind kind = getKind(transform);
            try {
                if (kind == SystemKind.BIN_TO_HEX) {
                    return decodeBinaryBody(transform.getBody(), HexFormat.of().parseHex(text), dest);
                }
                if (kind == SystemKind.BIN_TO_BASE64) {
                    return decodeBinaryBody(transform.getBody(), Base64.getDecoder().decode(text), dest);
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reverses bit_map: every placeholder of the body receives a boolean, the value of the bit at its position.
     * Bits beyond the available binary are {@code false} (trailing zero bytes can be trimmed by BIT_STRING).
     */
    private boolean decodeBitMap(EnotElement bitMap, byte[] data, Map<String, Object> dest) {
        ByteOrder byteOrder = bitMap.getAttribute(SystemAttribute.BYTE_ORDER) instanceof String byteOrderName
                ? ByteOrder.fromName(byteOrderName) : null;
        BitOrder bitOrder = bitMap.getAttribute(SystemAttribute.BIT_ORDER) instanceof String bitOrderName
                ? BitOrder.fromName(bitOrderName) : null;
        if (byteOrder == null || bitOrder == null) {
            return false;
        }

        Object body = bitMap.getBody();
        List<?> items = body instanceof List<?> listBody ? listBody : Collections.singletonList(body);
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (PlaceholderUtils.isPlaceholder(item)) {
                assignPlaceholder(item, isBitSet(data, i, byteOrder, bitOrder), dest);
            }
        }
        return true;
    }

    private boolean isBitSet(byte[] data, int bitIndex, ByteOrder byteOrder, BitOrder bitOrder) {
        int byteIndex = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? bitIndex / 8 : data.length - 1 - bitIndex / 8;
        if (byteIndex < 0 || byteIndex >= data.length) {
            return false;
        }
        int shift = BitOrder.LSB_FIRST.equals(bitOrder) ? bitIndex % 8 : 7 - bitIndex % 8;
        return (data[byteIndex] & (1 << shift)) != 0;
    }

    private String toIpAddress(byte[] data) {
        if (data.length != 4 && data.length != 16) {
            return null;
        }
        try {
            return InetAddress.getByAddress(data).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Attribute helpers
    // -------------------------------------------------------------------------

    private Asn1Tag getTag(EnotElement element) {
        Object tagValue = element.getAttributes().get(Asn1Attribute.TAG);
        if (tagValue instanceof String tagStr) {
            return Asn1Tag.fromString(tagStr);
        }
        return null;
    }

    private Integer getIntegerAttribute(EnotElement element, Asn1Attribute attribute) {
        Object value = element.getAttributes().get(attribute);
        return value instanceof Number number ? number.intValue() : null;
    }

    private SystemKind getKind(EnotElement element) {
        Object kindValue = element.getAttributes().get(SystemAttribute.KIND);
        if (kindValue instanceof String kindStr) {
            return SystemKind.fromString(kindStr);
        }
        return null;
    }
}
