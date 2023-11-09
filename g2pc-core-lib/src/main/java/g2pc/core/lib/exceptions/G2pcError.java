package g2pc.core.lib.exceptions;


import lombok.*;

/**
 * The type Error.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class G2pcError {

    private String code ;

    private String message;
}
