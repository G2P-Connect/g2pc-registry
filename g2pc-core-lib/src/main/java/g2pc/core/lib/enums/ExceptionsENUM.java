package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;
import java.io.IOException;

public enum ExceptionsENUM {


    ERROR_SIGNATURE_INVALID ,
    ERROR_VERSION_NOT_VALID ,
    ERROR_ENCRYPTION_INVALID ,
    ERROR_USER_UNAUTHORIZED ,
    ERROR_BAD_REQUEST ,
    ERROR_SERVICE_UNAVAILABLE ,

    ERROR_REQUEST_NOT_FOUND;

    public String toValue() {
        switch (this) {
            case ERROR_SIGNATURE_INVALID: return "err.signature.invalid";
            case ERROR_VERSION_NOT_VALID:  return "err.version.not_supported";
            case ERROR_ENCRYPTION_INVALID: return "err.encryption.invalid";
            case ERROR_USER_UNAUTHORIZED: return "err.request.unauthorized";
            case ERROR_BAD_REQUEST: return "err.request.bad";
            case ERROR_SERVICE_UNAVAILABLE: return "err.service.unavailable";
            case ERROR_REQUEST_NOT_FOUND: return "err.request.not_found";

        }
        return null;
    }

    public static ExceptionsENUM forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "err.signature.invalid": return ERROR_SIGNATURE_INVALID;
                case "err.version.not_supported": return ERROR_VERSION_NOT_VALID;
                case "err.encryption.invalid":return ERROR_ENCRYPTION_INVALID;
                case "err.request.unauthorized" :return ERROR_USER_UNAUTHORIZED;
                case "err.request.bad" :return ERROR_BAD_REQUEST;
                case "err.service.unavailable" : return ERROR_SERVICE_UNAVAILABLE;
                case "err.request.not_found" : return ERROR_REQUEST_NOT_FOUND ;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
