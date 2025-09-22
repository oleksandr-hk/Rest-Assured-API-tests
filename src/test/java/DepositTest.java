import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static constants.Constants.*;

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyOnHisOwnAccountTest() {
        final double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        //create user with randomly generated data
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        //create account for user
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword())
        );

        //deposit money on newly created account
        DepositSteps.depositAccount(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()),
                new CreateDepositRequest(createAccountResponse.getId(), createAccountResponse.getAccountNumber(), deposit)
        );

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
                            transaction.getRelatedAccountId() == createAccountResponse.getId() &&
                            transaction.getType().equals(DEPOSIT_TRANSACTION_TYPE);
                }));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(-1, "Invalid account or amount"),
                Arguments.of(MIN_NOT_VALID_DEPOSIT_AMOUNT, "Deposit amount exceeds the 5000 limit")
                );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void userCantDepositWrongAmountTest(double wrongBalance, String errorMessage) {
        //create user with randomly generated data
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        //create account for user
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(new LoginUserRequest(
                createUserRequest.getUsername(), createUserRequest.getPassword()
        ));
        //deposit money on newly created account
        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage),
                EndPoint.DEPOSIT
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
        final double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        //create user with randomly generated data
        CreateUserRequest firstUserRequest = AdminSteps.createUser();

        //create user with randomly generated data
        CreateUserRequest secondUserRequest = AdminSteps.createUser();

        //create account for second user
        CreateAccountResponse createSecondAccountResponse = AccountSteps.createAccountForUser(new LoginUserRequest(
                secondUserRequest.getUsername(), secondUserRequest.getPassword()
        ));

        //deposit money on another user account
        new CrudRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestForbidden(),
                EndPoint.DEPOSIT
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
