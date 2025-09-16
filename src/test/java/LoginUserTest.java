import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new LoginUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOk()
        ).post(loginUserRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create random user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest);

        //
        new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk()
        ).post(
                        LoginUserRequest.builder()
                                .username(createUserRequest.getUsername())
                                .password(createUserRequest.getPassword())
                                .build()
                ).assertThat()
                .header("Authorization", Matchers.notNullValue());
    }

}