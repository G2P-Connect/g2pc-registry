package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum AlgorithmENUM {

    ED25519, RSA;

    public String toValue() {
        switch (this) {
            case ED25519: return "Ed25519";
            case RSA: return "rsa";
        }
        return null;
    }

    public static AlgorithmENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "ed25519": return ED25519;
                case "rsa": return RSA;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
