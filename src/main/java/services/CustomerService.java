package services;

import models.CreateAccountResponse;
import models.CreateUserResponse;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Optional;


public class CustomerService {

    //get all customers
    public static List<CreateUserResponse> getCustomers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                        RequestSpecs.adminSpec(),
                        ResponseSpecs.requestReturnsOk(),
                        EndPoint.ADMIN_USER
                ).get();
    }

    //get customer by id
    public static Optional<CreateUserResponse> getCustomerById(long id) {
        return getCustomers().stream()
                .filter(customer -> customer.getId() == id)
                .findAny();
    }

    //get customer accounts
    public static List<CreateAccountResponse> getCustomerAccounts(String username, String password) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOk(),
                        EndPoint.CUSTOMER_ACCOUNTS
                ).get();
    }

    //get customer account by id
    public static Optional<CreateAccountResponse> getCustomerAccountById(String username, String password, long accountId) {
        return getCustomerAccounts(username, password).stream()
                .filter(account -> account.getId() == accountId)
                .findAny();
    }

}