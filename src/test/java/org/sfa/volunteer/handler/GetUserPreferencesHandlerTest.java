package org.sfa.volunteer.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.volunteer.dto.response.UserPreferenceResponse;
import org.sfa.volunteer.exception.UserNotFoundException;
import org.sfa.volunteer.service.UserService;
import org.sfa.volunteer.util.MessageSourceUtil;
import org.sfa.volunteer.util.ResponseBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserPreferencesHandlerTest {

    private static final String USER_ID = "SID-123";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private UserService userService;
    @Mock
    private MessageSourceUtil messageSourceUtil;

    private GetUserPreferencesHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageSourceUtil.getMessage(anyString(), any())).thenReturn("message");
        handler = new GetUserPreferencesHandler(userService, new ResponseBuilder(messageSourceUtil), messageSourceUtil);
    }

    private static APIGatewayProxyRequestEvent request(String method, String userId) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent().withHttpMethod(method);
        if (userId != null) {
            event.withPathParameters(Map.of("userId", userId));
        }
        return event;
    }

    private static JsonNode json(APIGatewayProxyResponseEvent response) throws Exception {
        return MAPPER.readTree(response.getBody());
    }

    private static UserPreferenceResponse savedPreferences() {
        return UserPreferenceResponse.builder()
                .userId(USER_ID)
                .userCategoryId(null)
                .userCategory(null)
                .timeZone("America/New_York")
                .language1Id(1L)
                .language1("English")
                .language2Id(2L)
                .language2("Spanish")
                .primaryEmail("secondary@example.com")
                .primaryPhone("123-456-7890")
                .build();
    }

    @Test
    void returnsPreferencesAndReturns200() throws Exception {
        when(userService.getUserPreferences(USER_ID)).thenReturn(savedPreferences());

        APIGatewayProxyResponseEvent response = handler.handleRequest(request("GET", USER_ID), null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Methods", "GET,OPTIONS");
        JsonNode body = json(response);
        assertThat(body.get("success").asBoolean()).isTrue();
        assertThat(body.at("/data/userId").asText()).isEqualTo(USER_ID);
        assertThat(body.at("/data/timeZone").asText()).isEqualTo("America/New_York");
        assertThat(body.at("/data/language1Id").asLong()).isEqualTo(1L);
        assertThat(body.at("/data/language1").asText()).isEqualTo("English");
        assertThat(body.at("/data/language2").asText()).isEqualTo("Spanish");
        assertThat(body.at("/data/primaryEmail").asText()).isEqualTo("secondary@example.com");
    }

    @Test
    void answersPreflightWithoutCallingService() {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("OPTIONS", USER_ID), null);

        assertThat(response.getStatusCode()).isEqualTo(204);
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Origin", "*");
        verifyNoInteractions(userService);
    }

    @Test
    void rejectsMissingUserId() throws Exception {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("GET", null), null);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(json(response).get("message").asText()).isEqualTo("userId path parameter is missing");
        verifyNoInteractions(userService);
    }

    @Test
    void returns404WhenUserDoesNotExist() throws Exception {
        when(userService.getUserPreferences(USER_ID)).thenThrow(new UserNotFoundException(USER_ID));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request("GET", USER_ID), null);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(json(response).get("success").asBoolean()).isFalse();
    }

    @Test
    void returns500OnUnexpectedError() throws Exception {
        when(userService.getUserPreferences(USER_ID)).thenThrow(new IllegalStateException("db down"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request("GET", USER_ID), null);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Origin");
    }
}
