package io.github.flexca.enot.core.util;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class OidUtils {

    private OidUtils() {
    }

    public static boolean isValidOid(Object input) {

        if(input instanceof String stringInput) {
            return isValidOid(stringInput);
        }
        return false;
    }

    public static boolean isValidOid(String input) {

        if (StringUtils.isBlank(input)) {
            return false;
        }
        try {
            ASN1ObjectIdentifier.tryFromID(input);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

}
