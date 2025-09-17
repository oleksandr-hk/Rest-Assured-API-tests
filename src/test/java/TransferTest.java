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

public class TransferTest {

    @Test
    public void userCanTransferMoneyOnHisOwnAccountTest() {
        int deposit = 25;
        int transferAmount = 10;
        int leftFirstOnAccount = 15;
        //create user with randomly generated data
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setRole("USER");
        CreateUserResponse createdUser = given()
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
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //create first account for user
        CreateAccountResponse firstAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        //create second account for user
        CreateAccountResponse secondAccountResponse = given()
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
        createDepositRequest.setId(firstAccountResponse.getId());
        createDepositRequest.setAccountNumber(firstAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(deposit);

        //deposit money on first created account
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
        Assertions.assertEquals(createDepositRequest.getBalance(), depositResponse.getBalance());

        //transfer money from first account to second one
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(firstAccountResponse.getId());
        transferRequest.setReceiverAccountId(secondAccountResponse.getId());
        transferRequest.setAmount(transferAmount);

        TransferResponse transferResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .when()
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(TransferResponse.class);

        //check first account balance
        Assertions.assertEquals(leftFirstOnAccount, CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        firstAccountResponse.getId()
                ).get()
                .getBalance());

        //check first account transactions list
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        firstAccountResponse.getId()
                ).stream().anyMatch(transaction -> {
                    return  transaction.getRelatedAccountId() == secondAccountResponse.getId()
                            && transaction.getAmount() == transferAmount;
                }));
        //check second account balance
        Assertions.assertEquals(transferAmount, CustomerService.getCustomerAccountById(createUserRequest.getUsername(), createUserRequest.getPassword(), secondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                secondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == firstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        }));
    }

    @Test
    public void userCanDepositMoneyOnAnotherUserAccountTest() {
        int deposit = 25;
        double transferAmount = 5.50;
        double leftOnFirstAccount = 19.50;
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

        //deposit money on first user account
        CreateDepositRequest createDepositRequest = new CreateDepositRequest();
        createDepositRequest.setId(createFirstAccountResponse.getId());
        createDepositRequest.setAccountNumber(createFirstAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(deposit);

        //deposit money on newly created account
        CreateAccountResponse depositResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization",  firstUserAuthToken)
                .when()
                .body(createDepositRequest)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CreateAccountResponse.class);

        //transfer money from first account to second one
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createFirstAccountResponse.getId());
        transferRequest.setReceiverAccountId(createSecondAccountResponse.getId());
        transferRequest.setAmount(transferAmount);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", firstUserAuthToken)
                .when()
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(TransferResponse.class);

        //check first account balance
        Assertions.assertEquals(leftOnFirstAccount, CustomerService.getCustomerAccountById(
                        firstUserRequest.getUsername(),
                        firstUserRequest.getPassword(),
                        createFirstAccountResponse.getId())
                .get()
                .getBalance());
        //check first account transactions list
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                firstUserRequest.getUsername(),
                firstUserRequest.getPassword(),
                createFirstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createSecondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        }));
        //check second account balance
        Assertions.assertEquals(transferAmount, CustomerService.getCustomerAccountById(
                        secondUserRequest.getUsername(),
                        secondUserRequest.getPassword(),
                        createSecondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                secondUserRequest.getUsername(),
                secondUserRequest.getPassword(),
                createSecondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createFirstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        }));
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(10,  0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10, -1, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10, 11, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCantTransferWrongAmountOfMoneyTest(int amountOfMoneyToDeposit, int amountMoneyToTransfer, String errorMessage) {
        //create user with randomly generated data
        String firstUserUsername = RandomStringUtils.randomAlphabetic(10);
        String firstUserUserPassword = "A" + RandomStringUtils.randomAlphabetic(5) + "7#^";
        CreateUserRequest firstUserRequest = new CreateUserRequest();
        firstUserRequest.setUsername(firstUserUsername);
        firstUserRequest.setPassword(firstUserUserPassword);
        firstUserRequest.setRole("USER");

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

        //generateAuth token for user
        LoginUserRequest loginFirstUserRequest = new LoginUserRequest();
        loginFirstUserRequest.setUsername(firstUserUsername);
        loginFirstUserRequest.setPassword(firstUserUserPassword);
        //extract header Authorization to variable
        String authToken = given()
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

        //create first account for user
        CreateAccountResponse createFirstAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        //create second account for user
        CreateAccountResponse createSecondAccountResponse = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(CreateAccountResponse.class);

        //deposit first account
        CreateDepositRequest createDepositRequest = new CreateDepositRequest();
        createDepositRequest.setId(createFirstAccountResponse.getId());
        createDepositRequest.setAccountNumber(createFirstAccountResponse.getAccountNumber());
        createDepositRequest.setBalance(amountOfMoneyToDeposit);

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
                .statusCode(HttpStatus.SC_OK);

        //transfer money from first account to second one
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createFirstAccountResponse.getId());
        transferRequest.setReceiverAccountId(createSecondAccountResponse.getId());
        transferRequest.setAmount(amountMoneyToTransfer);

        //try to transfer wrong amount of money between accounts
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .when()
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));

        //check if first account balance = deposit
        Assertions.assertEquals(amountOfMoneyToDeposit, CustomerService.getCustomerAccountById(
                firstUserRequest.getUsername(),
                firstUserRequest.getPassword(),
                createFirstAccountResponse.getId()
                ).get()
                .getBalance());

        //second account transactions list should be empty
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                firstUserRequest.getUsername(),
                firstUserRequest.getPassword(),
                createSecondAccountResponse.getId()
                )
                .isEmpty());
        //check if second account balance = 0
        Assertions.assertEquals(0, CustomerService.getCustomerAccountById(
                        firstUserRequest.getUsername(),
                        firstUserRequest.getPassword(),
                        createSecondAccountResponse.getId()
                ).get()
                .getBalance());
    }

}
