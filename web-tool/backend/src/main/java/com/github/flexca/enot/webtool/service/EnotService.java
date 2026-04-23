package com.github.flexca.enot.webtool.service;

import com.github.flexca.enot.core.Enot;
import com.github.flexca.enot.core.exception.EnotException;
import com.github.flexca.enot.core.parser.EnotInputFormat;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.util.BinaryUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class EnotService {

    private final Enot enot;
    private final ObjectMapper jsonObjectMapper;
    private final ObjectMapper yamlObjectMapper;

    public EnotService(Enot enot, @Qualifier("jsonObjectMapper") ObjectMapper jsonObjectMapper,
                       @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {

        this.enot = enot;
        this.jsonObjectMapper = jsonObjectMapper;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    public String serialize(String template, String params) throws EnotException {

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
        List<byte[]> result = enot.serialize(template, serializationContext);
        return Base64.getEncoder().encodeToString(BinaryUtils.concatenateBinary(result));
    }

    public String getExampleParams(String template, EnotInputFormat format) throws EnotException {

        Map<String, Object> example = enot.getParamsExample(template);
        if (EnotInputFormat.JSON.equals(format)) {
            return jsonObjectMapper.writeValueAsString(example);
        } else {
            return yamlObjectMapper.writeValueAsString(example);
        }
    }
}
