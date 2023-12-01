package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum LocalesENUM {

    EN, // English
    HI; // Hindi

    public String toValue() {
        switch (this) {
            case EN: return "en";
            case HI: return "hi";
        }
        return null;
    }

    public static LocalesENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "en": return EN;
                case "hi": return HI;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
