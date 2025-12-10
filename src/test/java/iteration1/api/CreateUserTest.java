package iteration1.api;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comaprison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    public void adminCanCreateUserWithCorrectData() {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        ResponseSpecs.entityWasCreated(),
                        EndPoint.ADMIN_USER
                        )
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest,createUserResponse).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of("   ", "Password33$", "USER", "username", List.of("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("abc%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );

    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithCorrectData(String username, String password, String role, String errorKey, List<String> errorValues) {
        //generate user data fom data provider
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
        //try to create user with invalid data
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadResponse(errorKey, errorValues),
                EndPoint.ADMIN_USER)
                .post(createUserRequest);
    }
}
