import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyOnHisOwnAccountTest() {
        final double deposit = 100.0;
        //create user with randomly generated data
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create account for user
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on newly created account
        CreateAccountResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(createAccountResponse.getId())
                .accountNumber(createAccountResponse.getAccountNumber())
                .balance(deposit)
                .build()
        ).extract().as(CreateAccountResponse.class);

        //check that user balance equal to new one
        CreateAccountResponse createdAccount = CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()
                )
                .get();
        //check if newly created user account deposit balance equal to expected
        Assertions.assertEquals(deposit, createdAccount.getBalance());

        //check that transactions contains required record
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        createAccountResponse.getId()).stream()
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
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create account for user
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on newly created account
        new DepositRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage)
        ).post(CreateDepositRequest.builder()
                .id(createAccountResponse.getId())
                .accountNumber(createAccountResponse.getAccountNumber())
                .balance(wrongBalance)
                .build()
        );
        //account transactions list should be empty
        softly.assertThat(TransactionService.getAccountTransactions(createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()).isEmpty()).isEqualTo(true);
        //check if account balance = 0
        softly.assertThat(0.0).isEqualTo(CustomerService.getCustomerAccountById(createUserRequest.getUsername(), createUserRequest.getPassword(), createAccountResponse.getId()).get().getBalance());
    }

    @Test
    public void userCantDepositMoneyOnDifferentUserAccountTest() {
        final int deposit = 100;
        //create user with randomly generated data
        CreateUserRequest firstUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create user with randomly generated data
        CreateUserRequest secondUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated first user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(firstUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create randomly generated second user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(secondUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create account for second user
        CreateAccountResponse createSecondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(secondUserRequest.getUsername(), secondUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on another user account
        new DepositRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestForbidden()
        ).post(CreateDepositRequest.builder()
                .id(createSecondAccountResponse.getId())
                .accountNumber(createSecondAccountResponse.getAccountNumber())
                .balance(deposit)
                .build()
        );
        //account transactions list should be empty for first account
        softly.assertThat(TransactionService.getAccountTransactions(secondUserRequest.getUsername(), secondUserRequest.getPassword(), createSecondAccountResponse.getId()).isEmpty()).isEqualTo(
                true);
        //account transactions list should be empty for second account
        softly.assertThat(TransactionService.getAccountTransactions(secondUserRequest.getUsername(), secondUserRequest.getPassword(), createSecondAccountResponse.getId()).isEmpty()).isEqualTo(true);
        //check if second user account balance equal to o
        softly.assertThat(CustomerService.getCustomerAccountById(secondUserRequest.getUsername(), secondUserRequest.getPassword(), createSecondAccountResponse.getId()).get().getBalance()).isEqualTo(0.0);
    }
}
