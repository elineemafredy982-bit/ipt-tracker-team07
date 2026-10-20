package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Optional on approve; mandatory (non-blank) on reject, enforced by the LogbookEntry entity. */
public record ReviewRequest(@Schema(example = "Please describe the tasks in more detail") @Size(max = 1000)
                            String comment) {
}
