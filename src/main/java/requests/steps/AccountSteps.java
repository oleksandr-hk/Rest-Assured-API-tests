package requests.steps;

import models.CreateAccountResponse;
import models.LoginUserRequest;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AccountSteps {

    public static CreateAccountResponse createAccountForUser(LoginUserRequest loginUserRequest) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(loginUserRequest.getUsername(), loginUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ACCOUNTS
        ).post(null);
    }
}
