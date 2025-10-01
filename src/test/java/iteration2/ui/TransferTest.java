package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateDepositRequest;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.steps.AccountSteps;
import requests.steps.DepositSteps;
import services.CustomerService;
import services.TransactionService;
import ui.pages.BankAlert;
import ui.pages.TransferPage;

import static constants.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransferTest extends BaseUITest {

    @Test
    @UserSession
    @DisplayName("User can transfer money between his own accounts")
    public void userCanTransferMoneyBetweenHisOwnAccountsTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, deposit);
        double leftFirstOnAccount = RandomData.roundDoubleValue(deposit - transferAmount, DEFAULT_DOUBLE_PRECISION);
        String recipientName = RandomData.getUsername();
        //create user with randomly generated data
        CreateUserRequest user = SessionStorage.getUser();

        //create first account for user
        CreateAccountResponse firstAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(user.getUsername(), user.getPassword()));

        //create second account for user
        CreateAccountResponse secondAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(user.getUsername(), user.getPassword()));

        //deposit money on first created account
        CreateAccountResponse depositResponse = DepositSteps.depositAccount(
                new LoginUserRequest(user.getUsername(), user.getPassword()),
                new CreateDepositRequest(firstAccountResponse.getId(), firstAccountResponse.getAccountNumber(), deposit)
        );

        //transfer money check alert and transactions page
        boolean containsInUITransactionList = new TransferPage().open().transfer(firstAccountResponse.getAccountNumber(), recipientName, secondAccountResponse.getAccountNumber(), transferAmount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), transferAmount, secondAccountResponse.getAccountNumber()))
                .openTransactionList()
                .getTransactions()
                .stream().anyMatch(transaction -> {
                        return transaction.getTransactionType().contains(TRANSFER_IN_TRANSACTION_TYPE)
                                && transaction.getAmount() == transferAmount;
                });
        assertTrue(containsInUITransactionList);
        //check API reflects changes
        //check first account balance
        softly.assertThat(leftFirstOnAccount).isEqualTo(
                CustomerService.getCustomerAccountById(
                                user.getUsername(),
                                user.getPassword(),
                                firstAccountResponse.getId()
                        ).get()
                        .getBalance());
        //check first account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                user.getUsername(),
                user.getPassword(),
                firstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == secondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_OUT_TRANSACTION_TYPE);
        })).isEqualTo(true);
        //check second account balance
        softly.assertThat(transferAmount).isEqualTo(CustomerService.getCustomerAccountById(user.getUsername(), user.getPassword(), secondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                user.getUsername(),
                user.getPassword(),
                secondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == firstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount
                    && transaction.getType().equals(TRANSFER_IN_TRANSACTION_TYPE);
        })).isEqualTo(true);
    }

    @Test
    @UserSession(value = 2, auth = 1)
    @DisplayName("User can transfer money to another user account")
    public void userCanTransferMoneyToAnotherUserAccountTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, deposit);
        double leftOnFirstAccount = RandomData.roundDoubleValue(deposit - transferAmount, DEFAULT_DOUBLE_PRECISION);
        //create first user
        CreateUserRequest firstUserRequest = SessionStorage.getUser(1);

        //create second user
        CreateUserRequest secondUserRequest = SessionStorage.getUser(2);

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

        //transfer money check alert and transactions page
        boolean containsInUITransactionList  = new TransferPage().open().transfer(createFirstAccountResponse.getAccountNumber(), secondUserRequest.getUsername(), createSecondAccountResponse.getAccountNumber(), transferAmount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), transferAmount, createSecondAccountResponse.getAccountNumber()))
                .openTransactionList()
                .getTransactions()
                .stream().anyMatch(transaction -> {
                    return transaction.getTransactionType().contains(TRANSFER_OUT_TRANSACTION_TYPE) && transaction.getAmount() == transferAmount  ;
                });
        assertTrue(containsInUITransactionList);

        //check API reflects changes
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

    @Test
    @UserSession
    @DisplayName("User can't transfer more then current deposit")
    public void userCantTransferMoreThanCurrentDepositTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.roundDoubleValue(deposit + 0.01, DEFAULT_DOUBLE_PRECISION);
        //create user with randomly generated data
        CreateUserRequest createUserRequest = SessionStorage.getUser();

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
                new CreateDepositRequest(firstAccountResponse.getId(), firstAccountResponse.getAccountNumber(), deposit)
        );

        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        //transfer money check alert and transactions page
        boolean containsInUITransactionList  = new TransferPage().open().transfer(firstAccountResponse.getAccountNumber(), createUserRequest.getUsername(), secondAccountResponse.getAccountNumber(), transferAmount)
                .checkAlertMessageAndAccept(String.format(BankAlert.NOT_VALID_TRANSFER_AMOUNT.getMessage(), transferAmount, secondAccountResponse.getAccountNumber()))
                .openTransactionList()
                .getTransactions()
                .stream().anyMatch(transaction -> {
                    return transaction.getTransactionType().contains(TRANSFER_OUT_TRANSACTION_TYPE) && transaction.getAmount() == transferAmount;
                });
        assertThat(containsInUITransactionList).isFalse();
        //check API reflects no changes
        //check if second account balance = 0
        softly.assertThat(0.0).isEqualTo(CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        secondAccountResponse.getId()
                ).get()
                .getBalance());
        //check second account balance
        softly.assertThat(0.0).isEqualTo(CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        secondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                secondAccountResponse.getId()
        ).isEmpty());
    }
}
