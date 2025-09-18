package services;

import models.CreateAccountResponse;
import models.CreateUserResponse;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;

public class CustomerService {

    //get all customers
    public static List<CreateUserResponse> getCustomers() {
        return Arrays.asList(given()
                .spec(RequestSpecs.adminSpec())
                .when()
                .get("/admin/users")
                .then()
                .assertThat()
                .spec(ResponseSpecs.requestReturnsOk())
                .extract()
                .as(CreateUserResponse[].class));
    }

    //get customer by id
    public static Optional<CreateUserResponse> getCustomerById(long id) {
        return getCustomers().stream()
                .filter(customer -> customer.getId() == id)
                .findAny();
    }

    //get customer accounts
    public static List<CreateAccountResponse> getCustomerAccounts(String username, String password) {
        return Arrays.asList(given()
                .spec(RequestSpecs.authAsUser(username, password))
                .when()
                .get("/customer/accounts")
                .then()
                .spec(ResponseSpecs.requestReturnsOk())
                .extract()
                .as(CreateAccountResponse[].class));
    }

    //get customer account by id
    public static Optional<CreateAccountResponse> getCustomerAccountById(String username, String password, int accountId) {
        return getCustomerAccounts(username, password).stream()
                .filter(account -> account.getId() == accountId)
                .findAny();
    }

}