package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static constants.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub/";
        Configuration.baseUrl = "http://192.168.0.165:3000";
        Configuration.browser = "chrome";
        //Configuration.browserSize = "1920x1080";
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true,
                        "enableLog", true)
        );
    }

    @Test
    @DisplayName("User can deposit money on his own account and transaction present in transaction")
    public void userCanDepositMoneyOnHisOwnAccountTest() {
        final double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        //create user with randomly generated data
        CreateUserRequest user = AdminSteps.createUser();

        //create account for user
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(user.getUsername(), user.getPassword())
        );
        //user login
        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        )
                .post(new LoginUserRequest(user.getUsername(), user.getPassword()))
                .extract().header("Authorization");
        //open dashboard page
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //open deposit page
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).shouldBe(Condition.visible);
        //select account and amount money to deposit
        $(Selectors.byCssSelector(".account-selector")).selectOptionContainingText(createAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(deposit));
        //submit deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();
        //Check submit alert and submit text
        //check alert content
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText.contains(String.valueOf(deposit)));
        assertThat(alertText.contains(createAccountResponse.getAccountNumber()));
        alert.accept();
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
        //create user with randomly generated data
        CreateUserRequest user = AdminSteps.createUser();

        //create account for user
        CreateAccountResponse createAccountResponse = AccountSteps.createAccountForUser(
                new LoginUserRequest(user.getUsername(), user.getPassword())
        );
        //user login
        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        )
                .post(new LoginUserRequest(user.getUsername(), user.getPassword()))
                .extract().header("Authorization");
        //open dashboard page
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //open deposit page
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).shouldBe(Condition.visible);
        //select account and amount money to deposit
        $(Selectors.byCssSelector(".account-selector")).selectOptionContainingText(createAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(deposit));
        //submit deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();
        //Check submit alert and submit text
        //check wrong alert content
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo("❌ Please enter a valid amount.");
        alert.accept();
        //check that API return new balance for newly created account
        CreateAccountResponse accountResponse = CustomerService.getCustomerAccountById(user.getUsername(), user.getPassword(), createAccountResponse.getId()).get();
        assertThat(accountResponse.getBalance()).isEqualTo(deposit);
        //check that transaction present in transaction list
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
