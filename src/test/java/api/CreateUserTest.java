package api;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comaprison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {
        //generate user data
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        //create user with previously generated data
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ADMIN_USER)
                .post(createUserRequest);

        //check if created user data equal to expected one
        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
//        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
//        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
//        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());
//        softly.assertAll();

    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of("   ", "Password33$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithCorrectData(String username, String password, String role, String errorKey, String errorValue) {
        //generate user data fom data provider
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
        //try to create user with invalid data
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadResponse(errorKey, errorValue),
                EndPoint.ADMIN_USER)
                .post(createUserRequest);
    }
}
