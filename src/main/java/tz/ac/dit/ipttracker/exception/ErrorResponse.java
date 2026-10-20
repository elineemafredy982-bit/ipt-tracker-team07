package tz.ac.dit.ipttracker.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

/** The single error shape returned by every failing request (Step 3). */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path,
                fieldErrors == null ? Map.of() : fieldErrors);
    }
}
