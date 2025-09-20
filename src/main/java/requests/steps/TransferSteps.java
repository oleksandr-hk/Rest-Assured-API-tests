package requests.steps;

import models.LoginUserRequest;
import models.TransferRequest;
import models.TransferResponse;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferSteps {

    public static TransferResponse transferMoney(LoginUserRequest loginUserRequest, TransferRequest transferRequest) {
        return new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authAsUser(loginUserRequest.getUsername(), loginUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.TRANSFER
        ).post(transferRequest);
    }
}
