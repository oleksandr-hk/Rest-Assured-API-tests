package requests.steps;

import models.*;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
