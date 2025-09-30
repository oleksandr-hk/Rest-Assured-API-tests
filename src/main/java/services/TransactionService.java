package services;

import api.models.Transaction;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class TransactionService {
    //get account transactions
    public static List<Transaction> getAccountTransactions(String username, String password, long accountId) {
        return new ValidatedCrudRequester<Transaction>(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.ACCOUNT_TRANSACTIONS
                ).getAllById(accountId);
    }
}
