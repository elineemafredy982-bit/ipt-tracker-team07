package tz.ac.dit.ipttracker.controller;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

final class Locations {
    private Locations() {
    }

    /** Builds the Location header value, e.g. of("/api/v1/organisations/{id}", 7). */
    static URI of(String pathTemplate, Object id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(pathTemplate).buildAndExpand(id).toUri();
    }
}
