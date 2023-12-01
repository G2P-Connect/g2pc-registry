package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum ActionsENUM {

    SEARCH, ON_SEARCH;

    public String toValue() {
        switch (this) {
            case SEARCH: return "search";
            case ON_SEARCH: return "on-search";
        }
        return null;
    }

    public static ActionsENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "search": return SEARCH;
                case "on-search": return ON_SEARCH;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
