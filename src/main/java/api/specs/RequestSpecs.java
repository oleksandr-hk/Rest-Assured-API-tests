package api.specs;

import api.configs.Config;
import api.models.LoginUserRequest;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.CrudRequester;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public class RequestSpecs {
    private static final Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters( List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter(), new SwaggerCoverageRestAssured(
                                new FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))), new AllureRestAssured())
                ).setBaseUri(Config.getProperty("apiBaseUrl"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder().addHeader("Authorization", "Basic YWRtaW46YWRtaW4=").build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaultRequestBuilder().addHeader("Authorization", getUserAuthHeader(username, password)).build();
    }

    public static String getUserAuthHeader(String username, String password) {
        String authToken = "";
        if (!authHeaders.containsKey(username)) {
            //extract header Authorization to variable
            authToken = new CrudRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnsOk(), EndPoint.LOGIN).post(new LoginUserRequest(username, password)).extract().header("Authorization");
            authHeaders.put(username, authToken);
        } else {
            authToken = authHeaders.get(username);
        }
        return authToken;
    }

}
