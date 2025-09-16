import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
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
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        //check that transactions contains required record
        softly.assertThat(depositResponse.getTransactions().stream()
                .anyMatch(transaction -> {
                    return  transaction.getAmount() == depositResponse.getBalance() &&
                            transaction.getRelatedAccountId() == createAccountResponse.getId();
                })).isEqualTo(true);
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
        CreateAccountResponse secondUserAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(secondUserRequest.getUsername(), secondUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on another user account
        new DepositRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestForbidden()
        ).post(CreateDepositRequest.builder()
                .id(secondUserAccount.getId())
                .accountNumber(secondUserAccount.getAccountNumber())
                .balance(deposit)
                .build()
        );
    }
}
