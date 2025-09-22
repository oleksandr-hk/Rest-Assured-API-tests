package services;

import models.TransactionResponse;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class TransactionService {
    //get account transactions
    public static List<TransactionResponse> getAccountTransactions(String username, String password, long accountId) {
        return new ValidatedCrudRequester<TransactionResponse>(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.ACCOUNT_TRANSACTIONS
                ).getAllById(accountId);
    }
}
