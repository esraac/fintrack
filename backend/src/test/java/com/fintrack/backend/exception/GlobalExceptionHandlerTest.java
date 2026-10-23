package com.fintrack.backend.exception;

import com.fintrack.backend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    @RestController
    static class ThrowingController {
        @PostMapping("/validate")
        void validate(@Valid @RequestBody RegisterRequest request) {}

        @GetMapping("/email-exists")
        void emailExists() { throw new EmailAlreadyExistsException(); }

        @GetMapping("/bad-credentials")
        void badCredentials() { throw new InvalidCredentialsException(); }

        @GetMapping("/integrity")
        void integrity() { throw new DataIntegrityViolationException("duplicate key ... users_email_key"); }

        @GetMapping("/boom")
        void boom() { throw new IllegalStateException("gizli ic detay"); }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validationError_returns400_withFieldNames() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"abc\",\"password\":\"kisa\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("email")))
                .andExpect(jsonPath("$.message", containsString("password")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{bozuk"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void emailExists_returns409() throws Exception {
        mvc.perform(get("/email-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void invalidCredentials_returns401() throws Exception {
        mvc.perform(get("/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void dataIntegrityViolation_returns409_withoutLeakingSql() throws Exception {
        mvc.perform(get("/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(content().string(not(containsString("users_email_key"))));
    }

    @Test
    void unexpectedException_returns500_withoutLeakingDetails() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("gizli ic detay"))));
    }

    @Test
    void wrongHttpMethod_returns405_notWrapped500() throws Exception {
        mvc.perform(get("/validate"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }
}