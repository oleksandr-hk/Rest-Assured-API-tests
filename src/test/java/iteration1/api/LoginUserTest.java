package iteration1.api;

import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        LoginUserResponse loginUserResponse = new ValidatedCrudRequester<LoginUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        ).post(loginUserRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        //create and generate user
        CreateUserRequest userResponse = AdminSteps.createUser();

        //check if user can generate auth token
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        ).post(
                        LoginUserRequest.builder()
                                .username(userResponse.getUsername())
                                .password(userResponse.getPassword())
                                .build()
                ).assertThat()
                .header("Authorization", Matchers.notNullValue());
    }

}