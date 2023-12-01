package g2pc.core.lib.exceptionhandler;

import g2pc.core.lib.exceptions.G2pcError;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The type Error response.
 */
@Data
@NoArgsConstructor
public class ValidationErrorResponse {
    private List<G2pcError> g2PcErrors;

    /**
     * Instantiates a new Error response.
     *
     * @param g2PcErrorList the error list
     */
    public ValidationErrorResponse(List<G2pcError> g2PcErrorList)
    {
        super();
        this.g2PcErrors = g2PcErrorList;
    }
}