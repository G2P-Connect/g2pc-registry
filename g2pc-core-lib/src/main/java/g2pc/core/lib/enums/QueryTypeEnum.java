package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum QueryTypeEnum {

    NAMEDQUERY , IDTYPE , PREDICATE ;

    public String toValue() {
        switch (this) {
            case NAMEDQUERY: return "namedQuery";
            case IDTYPE: return "idtype";
            case PREDICATE:return "predicate";
        }
        return null;
    }

    public static QueryTypeEnum forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "namedQuery": return NAMEDQUERY;
                case "idtype": return IDTYPE;
                case "predicate":return PREDICATE;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
