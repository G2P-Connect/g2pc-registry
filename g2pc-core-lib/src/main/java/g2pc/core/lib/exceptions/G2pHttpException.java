package g2pc.core.lib.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Getter
@Setter
public class G2pHttpException extends Exception{

    private G2pcError g2PcError;

    /**
     * Instantiates a new Validation exception.
     *
     * @param g2PcError the error list
     */
    public G2pHttpException(G2pcError g2PcError){
        this.g2PcError = g2PcError;

    }
}
