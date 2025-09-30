package requests.steps;


import api.models.CreateAccountResponse;
import api.models.CreateDepositRequest;
import api.models.LoginUserRequest;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class DepositSteps {
    public static CreateAccountResponse depositAccount(LoginUserRequest loginUserRequest, CreateDepositRequest createDepositRequest) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(loginUserRequest.getUsername(), loginUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.DEPOSIT
        ).post(CreateDepositRequest.builder()
                .id(createDepositRequest.getId())
                .accountNumber(createDepositRequest.getAccountNumber())
                .balance(createDepositRequest.getBalance())
                .build()
        );
    }
}
