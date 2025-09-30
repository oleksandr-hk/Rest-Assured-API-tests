package requests.steps;

import api.models.CreateAccountResponse;
import api.models.LoginUserRequest;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class AccountSteps {

    public static CreateAccountResponse createAccountForUser(LoginUserRequest loginUserRequest) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(loginUserRequest.getUsername(), loginUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ACCOUNTS
        ).post(null);
    }
}
