package services;

import models.TransactionResponse;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

public class TransactionService {

    //get account transactions
    public static List<TransactionResponse> getAccountTransactions(String username, String password, int accountId) {
        return Arrays.asList(given()
                .spec(RequestSpecs.authAsUser(username, password))
                .when()
                .pathParam("accountId", accountId)
                .get("/accounts/{accountId}/transactions")
                .then()
                .spec(ResponseSpecs.requestReturnsOk())
                .extract()
                .as(TransactionResponse[].class));
    }
}
