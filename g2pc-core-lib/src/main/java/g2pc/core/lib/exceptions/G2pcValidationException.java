package g2pc.core.lib.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * The type Validation exception.
 */
@Data
@Getter
@Setter
public class G2pcValidationException extends Exception{

    private List<G2pcError> g2PcErrorList;

    /**
     * Instantiates a new Validation exception.
     *
     * @param g2PcErrorList the error list
     */
    public G2pcValidationException(List<G2pcError> g2PcErrorList){
      this.g2PcErrorList = g2PcErrorList;

    }
}
