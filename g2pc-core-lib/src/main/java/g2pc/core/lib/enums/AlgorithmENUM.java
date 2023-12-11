package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;


/**
 * This enum class is used to return algorithm strings
 */
public enum AlgorithmENUM {

    ED25519, RSA , HMAC , SHA256 , AES;

    public String toValue() {
        switch (this) {
            case ED25519: return "Ed25519";
            case RSA: return "rsa";
            case HMAC:  return "HmacSHA256";
            case SHA256:return "SHA-256";
            case AES : return "AES";
        }
        return null;
    }

    public static AlgorithmENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "ed25519": return ED25519;
                case "rsa": return RSA;
                case "HmacSHA256" : return HMAC;
                case "SHA-256" : return SHA256;
                case "AES" : return AES;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
