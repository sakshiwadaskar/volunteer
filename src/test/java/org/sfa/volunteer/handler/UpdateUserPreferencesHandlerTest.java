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
import org.sfa.volunteer.dto.request.UserPreferenceRequest;
import org.sfa.volunteer.dto.response.UserPreferenceResponse;
import org.sfa.volunteer.exception.UserNotFoundException;
import org.sfa.volunteer.service.UserService;
import org.sfa.volunteer.util.MessageSourceUtil;
import org.sfa.volunteer.util.ResponseBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserPreferencesHandlerTest {

    private static final String USER_ID = "SID-123";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private UserService userService;
    @Mock
    private MessageSourceUtil messageSourceUtil;

    private UpdateUserPreferencesHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageSourceUtil.getMessage(anyString(), any())).thenReturn("message");
        handler = new UpdateUserPreferencesHandler(userService, new ResponseBuilder(messageSourceUtil), messageSourceUtil);
    }

    private static APIGatewayProxyRequestEvent request(String method, String userId, String body) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod(method)
                .withBody(body);
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
                .language1("French")
                .language2Id(2L)
                .language2("German")
                .language3Id(3L)
                .language3("Hindi")
                .primaryEmail("sec1@example.com")
                .secondaryEmail("sec2@example.com")
                .primaryPhone("111-111-1111")
                .secondaryPhone("222-222-2222")
                .build();
    }

    @Test
    void updatesPreferencesAndReturns200() throws Exception {
        when(userService.updateUserPreferences(eq(USER_ID), any(UserPreferenceRequest.class)))
                .thenReturn(savedPreferences());

        String requestBody = "{\"language1\":1,\"language2\":2,\"language3\":3,"
                + "\"secondaryEmail1\":\"sec1@example.com\",\"secondaryEmail2\":\"sec2@example.com\","
                + "\"secondaryPhone1\":\"111-111-1111\",\"secondaryPhone2\":\"222-222-2222\"}";

        APIGatewayProxyResponseEvent response = handler.handleRequest(request("PUT", USER_ID, requestBody), null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Methods", "PUT,OPTIONS");
        JsonNode body = json(response);
        assertThat(body.get("success").asBoolean()).isTrue();
        assertThat(body.at("/data/userId").asText()).isEqualTo(USER_ID);
        assertThat(body.at("/data/timeZone").asText()).isEqualTo("America/New_York");
        assertThat(body.at("/data/language1").asText()).isEqualTo("French");
        assertThat(body.at("/data/language2").asText()).isEqualTo("German");
        assertThat(body.at("/data/primaryEmail").asText()).isEqualTo("sec1@example.com");
    }

    @Test
    void decodesBase64Body() throws Exception {
        when(userService.updateUserPreferences(eq(USER_ID), any(UserPreferenceRequest.class)))
                .thenReturn(savedPreferences());
        String encoded = Base64.getEncoder()
                .encodeToString("{\"language1\":1}".getBytes(StandardCharsets.UTF_8));

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                request("PUT", USER_ID, encoded).withIsBase64Encoded(true), null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(json(response).at("/data/language1").asText()).isEqualTo("French");
    }

    @Test
    void answersPreflightWithoutCallingService() {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("OPTIONS", USER_ID, null), null);

        assertThat(response.getStatusCode()).isEqualTo(204);
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Origin", "*");
        verifyNoInteractions(userService);
    }

    @Test
    void rejectsMissingUserId() throws Exception {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("PUT", null, "{}"), null);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(json(response).get("message").asText()).isEqualTo("userId path parameter is missing");
        verifyNoInteractions(userService);
    }

    @Test
    void rejectsMissingBody() throws Exception {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("PUT", USER_ID, null), null);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(json(response).get("message").asText()).isEqualTo("Request body is required");
        verifyNoInteractions(userService);
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        APIGatewayProxyResponseEvent response = handler.handleRequest(request("PUT", USER_ID, "{not json"), null);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(json(response).get("message").asText()).isEqualTo("Invalid request body");
    }

    @Test
    void returns404WhenUserDoesNotExist() throws Exception {
        when(userService.updateUserPreferences(eq(USER_ID), any(UserPreferenceRequest.class)))
                .thenThrow(new UserNotFoundException(USER_ID));

        APIGatewayProxyResponseEvent response =
                handler.handleRequest(request("PUT", USER_ID, "{\"language1\":1}"), null);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(json(response).get("success").asBoolean()).isFalse();
    }

    @Test
    void returns500OnUnexpectedError() throws Exception {
        when(userService.updateUserPreferences(eq(USER_ID), any(UserPreferenceRequest.class)))
                .thenThrow(new IllegalStateException("db down"));

        APIGatewayProxyResponseEvent response =
                handler.handleRequest(request("PUT", USER_ID, "{\"language1\":1}"), null);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Origin");
    }
}
