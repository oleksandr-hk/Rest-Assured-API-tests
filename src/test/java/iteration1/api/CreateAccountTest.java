package iteration1.api;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
