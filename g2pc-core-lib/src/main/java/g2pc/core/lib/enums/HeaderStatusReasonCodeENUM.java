package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum HeaderStatusReasonCodeENUM {

    RJCT_VERSION_INVALID,
    RJCT_MESSAGE_ID_DUPLICATE,
    RJCT_MESSAGE_TS_INVALID,
    RJCT_ACTION_INVALID,
    RJCT_ACTION_NOT_SUPPORTED,
    RJCT_TOTAL_COUNT_INVALID,
    RJCT_TOTAL_COUNT_LIMIT_EXCEEDED,
    RJCT_ERRORS_TOO_MANY;

    public String toValue() {
        switch (this) {
            case RJCT_VERSION_INVALID:
                return "rjct.version.invalid";
            case RJCT_MESSAGE_ID_DUPLICATE:
                return "rjct.message_id.duplicate";
            case RJCT_MESSAGE_TS_INVALID:
                return "rjct.message_ts.invalid";
            case RJCT_ACTION_INVALID:
                return "rjct.action.invalid";
            case RJCT_ACTION_NOT_SUPPORTED:
                return "rjct.action.not_supported";
            case RJCT_TOTAL_COUNT_INVALID:
                return "rjct.total_count.invalid";
            case RJCT_TOTAL_COUNT_LIMIT_EXCEEDED:
                return "rjct.total_count.limit_exceeded";
            case RJCT_ERRORS_TOO_MANY:
                return "rjct.errors.too_many";
        }
        return null;
    }

    public static HeaderStatusReasonCodeENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "rjct.version.invalid":
                    return RJCT_VERSION_INVALID;
                case "rjct.message_id.duplicate":
                    return RJCT_MESSAGE_ID_DUPLICATE;
                case "rjct.message_ts.invalid":
                    return RJCT_MESSAGE_TS_INVALID;
                case "rjct.action.invalid":
                    return RJCT_ACTION_INVALID;
                case "rjct.action.not_supported":
                    return RJCT_ACTION_NOT_SUPPORTED;
                case "rjct.total_count.invalid":
                    return RJCT_TOTAL_COUNT_INVALID;
                case "rjct.total_count.limit_exceeded":
                    return RJCT_TOTAL_COUNT_LIMIT_EXCEEDED;
                case "rjct.errors.too_many":
                    return RJCT_ERRORS_TOO_MANY;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
