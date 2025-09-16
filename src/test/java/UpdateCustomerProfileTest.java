import io.restassured.http.ContentType;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileTest extends BaseTest {

    @Test
    public void customerCanChangeHisUsernameTest() {
        //create user with randomly generated data
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setRole("USER");
        CreateUserResponse createdUser = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(createUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"))
                .extract()
                .response()
                .as(CreateUserResponse.class);

        LoginUserRequest loginUserRequest = new LoginUserRequest();
        loginUserRequest.setUsername(username);
        loginUserRequest.setPassword(password);
        //extract header Authorization to variable
        String authToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        UpdateCustomerNameRequest updateCustomerNameRequest = new UpdateCustomerNameRequest();
        String newCustomerName = username + " " + username;
        updateCustomerNameRequest.setName(newCustomerName);
        //update customer name
        UpdateCustomerResponse updateCustomerResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", authToken)
                .body(updateCustomerNameRequest)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(UpdateCustomerResponse.class);

        //check that new username at response equal to expected one
        Assertions.assertEquals(newCustomerName, updateCustomerResponse.getCustomer().getName());
        Assertions.assertEquals( "Profile updated successfully", updateCustomerResponse.getMessage());
    }

    public static Stream<Arguments> userNameInvalidData() {
        return Stream.of(
                Arguments.of("Kate", "Name must contain two words with letters only"),
                Arguments.of(" ", "Name must contain two words with letters only"),
                Arguments.of("Kate P!! ", "Name must contain two words with letters only")
        );
    }

    @ParameterizedTest
    @MethodSource("userNameInvalidData")
    public void userCantUpdateUsernameWithInvalidDataTest(String newName, String errorMessage) {
        //create user with randomly generated data
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "A" + RandomStringUtils.randomAlphabetic(5) + "7#&^";
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setRole("USER");
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(createUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"))
                .extract()
                .response()
                .as(CreateUserResponse.class);

        LoginUserRequest loginUserRequest = new LoginUserRequest();
        loginUserRequest.setUsername(username);
        loginUserRequest.setPassword(password);
        //extract header Authorization to variable
        String authToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //update customer name with invalid data
        UpdateCustomerNameRequest updateCustomerNameRequest = new UpdateCustomerNameRequest();
        updateCustomerNameRequest.setName(newName);
        //update customer name with not valid data
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", authToken)
                .body(updateCustomerNameRequest)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));
    }
}
