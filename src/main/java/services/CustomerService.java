package services;

import io.restassured.http.ContentType;
import models.CreateAccountResponse;
import models.CreateUserResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;

public class CustomerService {

    //get all customers
    public static List<CreateUserResponse> getCustomers() {
        return Arrays.asList(given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CreateUserResponse[].class));
    }

    //get customer by id
    public static Optional<CreateUserResponse> getCustomerById(int id) {
        return getCustomers().stream()
                .filter(customer -> customer.getId() == id)
                .findAny();
    }

    //get customer accounts
    public static List<CreateAccountResponse> getCustomerAccounts(String username, String password) {
        //generate basis auth token
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        return Arrays.asList(given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic " + new String( encodedAuth))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
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
