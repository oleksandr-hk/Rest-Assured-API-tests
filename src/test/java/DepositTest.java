import io.restassured.http.ContentType;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import services.CustomerService;
import services.TransactionService;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyOnHisOwnAccountTest() {
        double deposit = 10.5;
        //create user with randomly generated data
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setRole("USER");
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(createUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"))
                .extract()
                .response()
                .as(CreateUserResponse.class);

        LoginUserRequest loginUserRequest = new LoginUserRequest();
        loginUserRequest.setUsername(username);
        loginUserRequest.setPassword(password);
        //extract header Authorization to variable
        String authToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .header("Authorization");

        //create account for user
        CreateAccountResponse createAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        CreateDepositRequest createDepositRequest = new CreateDepositRequest();
        createDepositRequest.setId(createAccountResponse.getId());
        createDepositRequest.setAccountNumber(createAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(deposit);

        //deposit money on newly created account
        CreateAccountResponse depositResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .when()
                .body(createDepositRequest)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CreateAccountResponse.class);

        //check that user balance equal to new one
        CreateAccountResponse createdAccount = CustomerService.getCustomerAccountById(
                    createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()
                )
                .get();
        //check if newly created user account deposit balance equal to expected
        Assertions.assertEquals(deposit, createdAccount.getBalance());

        //check that transactions contains required record
        Assertions.assertTrue(TransactionService.getAccountTransactions(createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()).stream()
                .anyMatch(transaction -> {
                    return  transaction.getAmount() == deposit &&
                            transaction.getRelatedAccountId() == createAccountResponse.getId();
                }));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(-1, "Invalid account or amount")
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void userCantDepositWrongAmountTest(int wrongBalance, String errorMessage) {
        //create user with randomly generated data
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setRole("USER");given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(createUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"));

        LoginUserRequest loginUserRequest = new LoginUserRequest();
        loginUserRequest.setUsername(username);
        loginUserRequest.setPassword(password);
        //extract header Authorization to variable
        String authToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //create account for user
        CreateAccountResponse createAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        CreateDepositRequest createDepositRequest = new CreateDepositRequest();
        createDepositRequest.setId(createAccountResponse.getId());
        createDepositRequest.setAccountNumber(createAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(wrongBalance);

        //deposit money on newly created account
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .when()
                .body(createDepositRequest)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));

        //account transactions list should be empty
        Assertions.assertTrue(TransactionService.getAccountTransactions(createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()).isEmpty());
        //check if account balance = 0
        Assertions.assertEquals(0, CustomerService.getCustomerAccountById(createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()).get().getBalance());
    }

    @Test
    public void userCantDepositMoneyOnDifferentUserAccountTest() {
        int deposit = 10;
        //create user with randomly generated data
        String firstUserUsername = RandomStringUtils.randomAlphabetic(10);
        String firstUserUserPassword = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest firstUserRequest = new CreateUserRequest();
        firstUserRequest.setUsername(firstUserUsername);
        firstUserRequest.setPassword(firstUserUserPassword);
        firstUserRequest.setRole("USER");

        String secondUserUsername = RandomStringUtils.randomAlphabetic(10);
        String secondUserUserPassword = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest secondUserRequest = new CreateUserRequest();
        secondUserRequest.setUsername(secondUserUsername);
        secondUserRequest.setPassword(secondUserUserPassword);
        secondUserRequest.setRole("USER");

        //create first user
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(firstUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(firstUserUsername))
                .body("password", Matchers.not(Matchers.equalTo(firstUserUserPassword)))
                .body("role", Matchers.equalTo("USER"));


        //create second user
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(secondUserRequest)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(secondUserUsername))
                .body("password", Matchers.not(Matchers.equalTo(secondUserUserPassword)))
                .body("role", Matchers.equalTo("USER"));

        //generateAuth token for first user
        LoginUserRequest loginFirstUserRequest = new LoginUserRequest();
        loginFirstUserRequest.setUsername(firstUserUsername);
        loginFirstUserRequest.setPassword(firstUserUserPassword);
        //extract header Authorization to variable
        String firstUserAuthToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginFirstUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //generate auth token for second user
        LoginUserRequest loginSecondUserRequest = new LoginUserRequest();
        loginSecondUserRequest.setUsername(secondUserUsername);
        loginSecondUserRequest.setPassword(secondUserUserPassword);
        //extract header Authorization to variable
        String secondUserAuthToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body(loginSecondUserRequest)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //create account for first user
        CreateAccountResponse createFirstAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", firstUserAuthToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        //create account for second user
        CreateAccountResponse createSecondAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", secondUserAuthToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);


        CreateDepositRequest createDepositRequest = new CreateDepositRequest();
        createDepositRequest.setId(createSecondAccountResponse.getId());
        createDepositRequest.setAccountNumber(createSecondAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(deposit);

        //deposit money on newly created account
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", firstUserAuthToken)
                .when()
                .body(createDepositRequest)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        //account transactions list should be empty for first account
        Assertions.assertTrue(TransactionService.getAccountTransactions(firstUserRequest.getUsername(), firstUserRequest.getPassword(), createFirstAccountResponse.getId()).isEmpty());
        //account transactions list should be empty for second account
        Assertions.assertTrue(TransactionService.getAccountTransactions(secondUserRequest.getUsername(), secondUserRequest.getPassword(), createSecondAccountResponse.getId()).isEmpty());
        //check if second user account balance equal to o
        Assertions.assertEquals(0, CustomerService.getCustomerAccountById(secondUserRequest.getUsername(), secondUserRequest.getPassword(), createSecondAccountResponse.getId()).get().getBalance());
    }
}
