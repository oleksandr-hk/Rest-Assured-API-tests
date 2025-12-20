package api.requests.steps;

import api.models.CreateAccountResponse;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return StepLogger.log("User " + username + " get all accounts", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                    RequestSpecs.authAsUser(this.username, this.password),
                    ResponseSpecs.requestReturnsOk(),
                    EndPoint.CUSTOMER_ACCOUNTS
            ).getAll(CreateAccountResponse[].class);
        });
    }
}
