package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum SortOrderEnum {

    ASC , DESC ;

    public String toValue() {
        switch (this) {
            case ASC: return "asc";
            case DESC: return "desc";
        }
        return null;
    }

    public static SortOrderEnum forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "asc": return ASC;
                case "desc": return DESC;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
