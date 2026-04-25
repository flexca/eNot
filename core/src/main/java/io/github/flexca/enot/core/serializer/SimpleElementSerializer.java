package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.List;

/**
 * Convenience base for serializers that follow a two-phase pattern:
 * <ol>
 *   <li><b>Body traversal</b> — the full element body is serialized recursively by
 *       {@link BaseElementSerializer#serializeBody}, producing a flat list of
 *       {@link ElementSerializationResult} fragments.</li>
 *   <li><b>Encoding</b> — the subclass receives the pre-serialized fragments and
 *       decides how to combine them (e.g. wrap in a DER container, pack bits into a
 *       byte array, compute a hash).</li>
 * </ol>
 *
 * <p>Most leaf and container serializers extend this class.
 * Serializers that need more control over body traversal (such as LOOP, which must
 * advance the context iterator per iteration) extend {@link BaseElementSerializer}
 * directly instead.</p>
 */
public abstract class SimpleElementSerializer extends BaseElementSerializer {

    /**
     * Encodes the element using the already-serialized body fragments.
     *
     * <p>Called by {@link #serialize(EnotElement, SerializationContext, String, EnotContext)}
     * after body traversal is complete. The list may be empty if all body children
     * were optional and absent.</p>
     *
     * @param element        the element being serialized (provides attributes)
     * @param serializedBody the pre-serialized body fragments in document order
     * @param jsonPath       JSON Pointer to the {@code body} node, used in error messages
     * @return the encoded output fragments; never {@code null}
     * @throws EnotSerializationException if encoding fails
     */
    protected abstract List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                                  String jsonPath) throws EnotSerializationException;

    /**
     * Template-method implementation: serializes the body, then delegates to
     * {@link #serialize(EnotElement, List, String)}.
     */
    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                                      EnotContext enotContext) throws EnotSerializationException {

        String currentJsonPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        List<ElementSerializationResult> serializedBody = serializeBody(element.getBody(), context, currentJsonPath, enotContext);
        return serialize(element, serializedBody, currentJsonPath);
    }
}
