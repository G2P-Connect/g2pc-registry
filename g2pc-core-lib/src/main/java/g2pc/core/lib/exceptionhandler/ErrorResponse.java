package g2pc.core.lib.exceptionhandler;

import g2pc.core.lib.exceptions.G2pcError;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private G2pcError g2PcError;

    /**
     * Instantiates a new Error response.
     *
     * @param g2PcError the error list
     */
    public ErrorResponse(G2pcError g2PcError)
    {
        super();
        this.g2PcError = g2PcError;
    }
}
