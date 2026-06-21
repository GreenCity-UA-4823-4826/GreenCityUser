package greencity.controller;

import greencity.config.SecurityConfig;
import greencity.security.jwt.JwtTool;
import greencity.service.EmailService;
import greencity.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class)
@ContextConfiguration(classes = {
    EmailControllerSecurityTest.TestApplication.class,
    EmailController.class,
    SecurityConfig.class
})
class EmailControllerSecurityTest {
    private static final String ADD_ECO_NEWS_URL = "/email/addEcoNews";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtTool jwtTool;

    @MockBean
    private UserService userService;

    @SpringBootConfiguration
    static class TestApplication {
    }

    @Test
    void addEcoNewsWithoutAuthorizationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(ADD_ECO_NEWS_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(getRequestBody()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(emailService);
    }

    @Test
    void addEcoNewsWithAuthorizedUserReturnsOk() throws Exception {
        mockMvc.perform(post(ADD_ECO_NEWS_URL)
            .with(user("test1@mail.com").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(getRequestBody()))
            .andExpect(status().isOk());
    }

    private String getRequestBody() {
        return """
            {
              "author": {
                "email": "test1@mail.com",
                "id": 154,
                "name": "Test1"
              },
              "creationDate": "2023-08-23T11:46:06.482Z",
              "imagePath": "string",
              "source": "string",
              "text": "Test1241254125125125124",
              "title": "Test1111",
              "unsubscribeToken": "string"
            }
            """;
    }
}
