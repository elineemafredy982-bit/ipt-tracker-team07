package tz.ac.dit.ipttracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tz.ac.dit.ipttracker.dto.OrganisationResponse;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.security.JwtService;
import tz.ac.dit.ipttracker.service.OrganisationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganisationController.class)
class OrganisationControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean OrganisationService service;
    @MockitoBean JwtService jwtService;   // required by the JwtAuthenticationFilter bean

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenNameBlank_returns400WithStandardErrorShape() throws Exception {
        mvc.perform(post("/api/v1/organisations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"sector\":\"ICT\",\"region\":\"Arusha\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/organisations"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void get_whenOrganisationMissing_returns404WithStandardErrorShape() throws Exception {
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("Organisation 99 not found"));

        mvc.perform(get("/api/v1/organisations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organisation 99 not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/organisations/99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenValid_returns201WithLocationHeader() throws Exception {
        when(service.create(any())).thenReturn(
                new OrganisationResponse(7L, "TTCL", "ICT", "Dar es Salaam", null, null));

        mvc.perform(post("/api/v1/organisations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TTCL\",\"sector\":\"ICT\",\"region\":\"Dar es Salaam\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/organisations/7")))
                .andExpect(jsonPath("$.id").value(7));
    }
}
