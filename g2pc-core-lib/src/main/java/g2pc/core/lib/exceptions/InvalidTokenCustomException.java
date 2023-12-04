package g2pc.core.lib.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
/**
 * The type Token is not valid exception.
 */
public class InvalidTokenCustomException extends Exception{

    private G2pcError g2PcError;

    /**
     * Instantiates a new Token is not valid exception.
     *
     * @param g2PcError the message
     */
    public InvalidTokenCustomException(G2pcError g2PcError){
        this.g2PcError = g2PcError;

    }
}
