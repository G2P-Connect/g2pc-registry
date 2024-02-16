package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum StatusTransactionTypeEnum {


    /**
     * "search" "subscribe" "unsubscribe"
     */

    SEARCH , SUBSCRIBE , UNSUBSCRIBE , ON_SEARCH , ON_SUBSCRIBE , ON_UNSUBSCRIBE;


    public String toValue() {
        switch (this) {
            case SEARCH:
                return "search";
            case SUBSCRIBE:
                return "subscribe";
            case UNSUBSCRIBE:
                return "unsubscribe";
            case ON_SEARCH:
                return "on-search";
            case ON_SUBSCRIBE:
                return "on-subscribe";
            case ON_UNSUBSCRIBE:
                return "on-unsubscribe";
        }
        return null;
    }

    public static StatusTransactionTypeEnum forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "search":
                    return SEARCH;
                case "subscribe":
                    return SUBSCRIBE;
                case "unsubscribe":
                    return UNSUBSCRIBE;
                case "on-search":
                    return ON_SEARCH;
                case "on-subscribe":
                    return ON_SUBSCRIBE;
                case "on-unsubscribe":
                    return ON_UNSUBSCRIBE;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
