package tz.ac.dit.ipttracker.security;

import tz.ac.dit.ipttracker.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** Writes the Step 3 error shape for failures raised inside the filter chain (401 / 403). */
final class SecurityErrorWriter {
    private SecurityErrorWriter() {
    }

    static void write(ObjectMapper mapper, HttpServletRequest req, HttpServletResponse res, HttpStatus status,
                      String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(),
                ErrorResponse.of(status, message, req.getRequestURI(), null));
    }
}
