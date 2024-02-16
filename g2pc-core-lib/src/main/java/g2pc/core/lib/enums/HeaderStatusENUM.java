package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum HeaderStatusENUM {

    RCVD,
    PDNG,
    SUCC,
    RJCT;

    public String toValue() {
        switch (this) {
            case RCVD:
                return "rcvd";
            case PDNG:
                return "pdng";
            case SUCC:
                return "succ";
            case RJCT:
                return "rjct";
        }
        return null;
    }

    public static HeaderStatusENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "rcvd":
                    return RCVD;
                case "pdng":
                    return PDNG;
                case "succ":
                    return SUCC;
                case "rjct":
                    return RJCT;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
