import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import requests.steps.TransferSteps;
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static constants.Constants.*;

public class TransferTest extends BaseTest{

    @Test
    public void userCanTransferMoneyOnHisOwnAccountTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, deposit);
        double leftFirstOnAccount = RandomData.roundDoubleValue(deposit - transferAmount, DEFAULT_DOUBLE_PRECISION);
        //create user with randomly generated data
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        //create first account for user
        CreateAccountResponse firstAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()));

        //create second account for user
        CreateAccountResponse secondAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()));

        //deposit money on first created account
        CreateAccountResponse depositResponse = DepositSteps.depositAccount(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()),
                new CreateDepositRequest(firstAccountResponse.getId(), firstAccountResponse.getAccountNumber(), deposit)
        );

        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        //transfer money from first account to second one
        TransferSteps.transferMoney(new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()),
                new TransferRequest(
                        firstAccountResponse.getId(),
                        secondAccountResponse.getId(),
                        transferAmount
                ));

        //check first account balance
        softly.assertThat(leftFirstOnAccount).isEqualTo(
                        CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        firstAccountResponse.getId()
                ).get()
                .getBalance());

        //check first account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                firstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == secondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_OUT_TRANSACTION_TYPE);
        })).isEqualTo(true);
        //check second account balance
        softly.assertThat(transferAmount).isEqualTo(CustomerService.getCustomerAccountById(createUserRequest.getUsername(), createUserRequest.getPassword(), secondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                secondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == firstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_IN_TRANSACTION_TYPE);
        })).isEqualTo(true);
    }

    @Test
    public void userCanTransferMoneyOnAnotherUserAccountTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, deposit);
        double leftOnFirstAccount = RandomData.roundDoubleValue(deposit - transferAmount, DEFAULT_DOUBLE_PRECISION);
        //create first user
        CreateUserRequest firstUserRequest = AdminSteps.createUser();

        //create second user
        CreateUserRequest secondUserRequest = AdminSteps.createUser();

        //create account for first user
        CreateAccountResponse createFirstAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(firstUserRequest.getUsername(), firstUserRequest.getPassword())
        );

        //create account for second user
        CreateAccountResponse createSecondAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(secondUserRequest.getUsername(), secondUserRequest.getPassword())
        );

        DepositSteps.depositAccount(
                new LoginUserRequest(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                CreateDepositRequest.builder()
                        .id(createFirstAccountResponse.getId())
                        .accountNumber(createFirstAccountResponse.getAccountNumber())
                        .balance(deposit)
                        .build()
        );

        //transfer money from first to second account
        TransferSteps.transferMoney(new LoginUserRequest(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                TransferRequest.builder()
                        .senderAccountId(createFirstAccountResponse.getId())
                        .receiverAccountId(createSecondAccountResponse.getId())
                        .amount(transferAmount).build());

        //check first account balance
        softly.assertThat(leftOnFirstAccount).isEqualTo(CustomerService.getCustomerAccountById(
                        firstUserRequest.getUsername(),
                        firstUserRequest.getPassword(),
                        createFirstAccountResponse.getId())
                .get()
                .getBalance());

        //check first account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                firstUserRequest.getUsername(),
                firstUserRequest.getPassword(),
                createFirstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createSecondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_OUT_TRANSACTION_TYPE);
        })).isEqualTo(true);
        //check second account balance
        softly.assertThat(transferAmount).isEqualTo(CustomerService.getCustomerAccountById(
                        secondUserRequest.getUsername(),
                        secondUserRequest.getPassword(),
                        createSecondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                secondUserRequest.getUsername(),
                secondUserRequest.getPassword(),
                createSecondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createFirstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_IN_TRANSACTION_TYPE);
        })).isEqualTo(true);

    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(10.0,  0.0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10.0, -1.0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10.0, 11.0, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCantTransferWrongAmountOfMoneyTest(double amountOfMoneyToDeposit, double amountMoneyToTransfer, String errorMessage) {
        //create user with randomly generated data
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        //create first account for user
        CreateAccountResponse firstAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword())
        );

        //create second account for user
        CreateAccountResponse secondAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()));

        //deposit money on first created account
        CreateAccountResponse depositResponse = DepositSteps.depositAccount(
                new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()),
                new CreateDepositRequest(firstAccountResponse.getId(), firstAccountResponse.getAccountNumber(), amountOfMoneyToDeposit)
        );

        //check that user balance equal to new one
        softly.assertThat(amountOfMoneyToDeposit).isEqualTo(depositResponse.getBalance());

        //try to transfer wrong amount of money between accounts
        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage),
                EndPoint.TRANSFER
        ).post(new TransferRequest(
                firstAccountResponse.getId(),
                secondAccountResponse.getId(),
                amountMoneyToTransfer
        ));

        //check if second account balance = 0
        softly.assertThat(0.0).isEqualTo(CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        secondAccountResponse.getId()
                ).get()
                .getBalance());
    }
}
