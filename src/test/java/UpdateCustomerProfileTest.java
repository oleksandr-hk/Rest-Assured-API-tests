import generators.RandomData;
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
import requests.AdminCreateUserRequester;
import requests.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileTest extends BaseTest {

    @Test
    public void customerCanChangeHisUsernameTest() {
        //create user with randomly generated data
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        String newCustomerName = RandomData.getUsername() + " " + RandomData.getUsername();
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerProfileRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).put(UpdateCustomerNameRequest.builder().name(newCustomerName).build())
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
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //update customer name with invalid data
        new UpdateCustomerProfileRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage)
        ).put(UpdateCustomerNameRequest.builder().name(newName).build());
    }
}
