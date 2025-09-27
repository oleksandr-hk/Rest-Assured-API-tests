package iteration1.api;

import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.api.Test;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        //create and generate user
        CreateUserRequest userResponse = AdminSteps.createUser();

        //creating account for newly generated user
        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUser(userResponse.getUsername(), userResponse.getPassword()),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ACCOUNTS
        ).post(null);
    }
}
