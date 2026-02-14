package tr.gov.tuketbir.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Access Denied Exception - 403
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    private String resource;
    private String action;

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String resource, String action) {
        super(String.format("Bu işlem için yetkiniz bulunmamaktadır: %s - %s", resource, action));
        this.resource = resource;
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }
}
