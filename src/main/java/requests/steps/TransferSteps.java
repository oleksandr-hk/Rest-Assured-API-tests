package requests.steps;

import api.models.LoginUserRequest;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class TransferSteps {

    public static TransferResponse transferMoney(LoginUserRequest loginUserRequest, TransferRequest transferRequest) {
        return new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authAsUser(loginUserRequest.getUsername(), loginUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.TRANSFER
        ).post(transferRequest);
    }
}
