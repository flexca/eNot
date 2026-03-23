package com.github.flexca.enot.core.struct;

import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import lombok.Getter;

import java.util.Set;

public interface EnotElementSpecification {

    ValueSpecification getConsumeType();

    ValueSpecification getProduceType();

    Set<Asn1Attribute> getRequiredAttributes();

    Set<Asn1Attribute> getAllowedAttributes();
}
