package g2pc.core.lib.exceptionhandler;

import g2pc.core.lib.exceptions.G2pcValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Global exception handler.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle exception error response.
     *
     * @param ex the ValidationExeption
     * @return the error response
     */
    @ExceptionHandler(value
                      = G2pcValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ValidationErrorResponse
    handleException(G2pcValidationException ex)
    {
        return new ValidationErrorResponse(ex.getG2PcErrorList());
    }
}