package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.requests.steps.AdminSteps;

import iteration1.ui.BaseUITest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import requests.steps.AccountSteps;
import services.CustomerService;
import services.TransactionService;
import ui.pages.BankAlert;
import ui.pages.DepositPage;

import static constants.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUITest {

    @Test
    @DisplayName("User can deposit money on his own account and transaction present in transaction")
    public void userCanDepositMoneyOnHisOwnAccountTest() {
        final double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(new LoginUserRequest(user.getUsername(), user.getPassword()));
        authAsUther(user);
        new DepositPage().open().depositAccount(createAccountResponse.getAccountNumber(), deposit)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage(), deposit, createAccountResponse.getAccountNumber()));

        //check that API return new balance for newly created account
        CreateAccountResponse accountResponse = CustomerService.getCustomerAccountById(user.getUsername(), user.getPassword(), createAccountResponse.getId()).get();
        assertThat(accountResponse.getBalance()).isEqualTo(deposit);
        //check that deposit transaction is present in transaction list
        Assertions.assertTrue(TransactionService.getAccountTransactions(
                        user.getUsername(),
                        user.getPassword(),
                        createAccountResponse.getId()).stream()
                .anyMatch(transaction -> {
                    return  transaction.getAmount() == deposit &&
                            transaction.getRelatedAccountId() == createAccountResponse.getId() &&
                            transaction.getType().equals(DEPOSIT_TRANSACTION_TYPE);
                }));
    }

    @Test
    @DisplayName("User can't deposit 0 or -1 amount of money on deposit")
    public void userCantDepositMoneyOnHisOwnAccountTest() {
        final double deposit = 0;
        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(new LoginUserRequest(user.getUsername(), user.getPassword()));
        authAsUther(user);
        new DepositPage().open().depositAccount(createAccountResponse.getAccountNumber(), deposit)
                .checkAlertMessageAndAccept(String.format(BankAlert.NOT_VALID_AMOUNT.getMessage(), deposit, createAccountResponse.getAccountNumber()));
        //check that API return 0 balance for newly created account
        CreateAccountResponse accountResponse = CustomerService.getCustomerAccountById(user.getUsername(), user.getPassword(), createAccountResponse.getId()).get();
        assertThat(accountResponse.getBalance()).isEqualTo(deposit);
        //check that transaction is absent in transaction list
        assertThat(TransactionService.getAccountTransactions(
                        user.getUsername(),
                        user.getPassword(),
                        createAccountResponse.getId()).stream()
                .anyMatch(transaction -> {
                    return  transaction.getAmount() == deposit &&
                            transaction.getRelatedAccountId() == createAccountResponse.getId() &&
                            transaction.getType().equals(DEPOSIT_TRANSACTION_TYPE);
                })).isFalse();
    }
}
