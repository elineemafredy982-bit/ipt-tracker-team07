package tz.ac.dit.ipttracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs against a real PostgreSQL 16 started by Testcontainers, so the Flyway migrations are tested too. */
@SpringBootTest(properties = "app.jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2")
@AutoConfigureMockMvc
@Testcontainers
class LogbookFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    private long placementIdOf(String token) throws Exception {
        String body = mvc.perform(get("/api/v1/placements").header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.content[0].id")).longValue();
    }

    private String entryJson(LocalDate date) {
        return "{\"entryDate\":\"" + date + "\",\"tasksDone\":\"Network setup\",\"skillsLearned\":\"VLANs\","
                + "\"hoursWorked\":6}";
    }

    @Test
    void fullFlow_loginCreateSubmitApproveAndSummary() throws Exception {
        String student = login("student1@ipt.test");
        String supervisor = login("industrial@ipt.test");
        long placementId = placementIdOf(student);

        String created = mvc.perform(post("/api/v1/placements/" + placementId + "/entries")
                        .header("Authorization", student).contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson(LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        long entryId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mvc.perform(post("/api/v1/entries/" + entryId + "/submit").header("Authorization", student))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUBMITTED"));

        mvc.perform(post("/api/v1/entries/" + entryId + "/approve").header("Authorization", supervisor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(get("/api/v1/placements/" + placementId + "/summary").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(1))
                .andExpect(jsonPath("$.totalApprovedHours").value(6));
    }

    @Test
    void createEntry_whenDateAlreadyUsed_returns422WithClearMessage() throws Exception {
        String student = login("student1@ipt.test");
        long placementId = placementIdOf(student);
        LocalDate day = LocalDate.now().minusDays(1);

        mvc.perform(post("/api/v1/placements/" + placementId + "/entries").header("Authorization", student)
                .contentType(MediaType.APPLICATION_JSON).content(entryJson(day))).andExpect(status().isCreated());

        mvc.perform(post("/api/v1/placements/" + placementId + "/entries").header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(entryJson(day)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("An entry already exists for " + day));
    }

    @Test
    void getEntries_whenStudentReadsSomeoneElsesPlacement_returns403() throws Exception {
        long placementId = placementIdOf(login("student1@ipt.test"));

        mvc.perform(get("/api/v1/placements/" + placementId + "/entries")
                        .header("Authorization", login("student2@ipt.test")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void anyEndpoint_withoutToken_returns401WithStandardErrorShape() throws Exception {
        mvc.perform(get("/api/v1/placements"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/placements"));
    }

    @Test
    void health_isPublicAndReportsUp() throws Exception {
        String body = mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("\"status\":\"UP\"");
    }
}
