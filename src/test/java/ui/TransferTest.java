package ui;

import api.BaseTest;
import com.codeborne.selenide.*;
import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateDepositRequest;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static constants.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseTest {
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
    @DisplayName("User can transfer money between his own accounts")
    public void userCanTransferMoneyBetweenHisOwnAccountsTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, deposit);
        double leftFirstOnAccount = RandomData.roundDoubleValue(deposit - transferAmount, DEFAULT_DOUBLE_PRECISION);
        String recipientName = RandomData.getUsername();
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
        //user login
        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        )
                .post(new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .extract().header("Authorization");
        //open dashboard page
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //open transfer page
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).shouldBe(Condition.visible);
        //select account and amount money to deposit
        $(Selectors.byCssSelector(".account-selector")).selectOptionContainingText(firstAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(recipientName);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transferAmount));
        $(Selectors.byId("confirmCheck")).click();
        //submit transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();
        //Check submit alert and submit text
        Alert alert = switchTo().alert();
        //check alert content
        String alertText = alert.getText();
        assertThat(alertText.contains(String.valueOf(deposit)));
        assertThat(alertText.contains(secondAccountResponse.getAccountNumber()));
        alert.accept();
        //navigate to transfer history page and check that transfer present
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        $(Selectors.byText("\uD83D\uDD0D Search Transactions")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter name to find transactions")).sendKeys(createUserRequest.getUsername());
        ElementsCollection transactionList = $(Selectors.byCssSelector("li.list-group-item")).findAll("span");
        boolean containsInUITransactionList = transactionList.stream().map(SelenideElement::getText).anyMatch(transactionText -> {
                    return transactionText.contains(createUserRequest.getUsername()) &&
                        transactionText.contains(TRANSFER_IN_TRANSACTION_TYPE) && transactionText.contains(String.valueOf(transferAmount))  ;
                });
        assertThat(containsInUITransactionList);
        //check API reflects changes
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
    @DisplayName("User can transfer money to another user account")
    public void userCanTransferMoneyToAnotherUserAccountTest() {
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

        //user login
        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        )
                .post(new LoginUserRequest(firstUserRequest.getUsername(), firstUserRequest.getPassword()))
                .extract().header("Authorization");
        //open dashboard page
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //open transfer page
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).shouldBe(Condition.visible);
        //select account and amount money to deposit
        $(Selectors.byCssSelector(".account-selector")).selectOptionContainingText(createFirstAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(secondUserRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(createSecondAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transferAmount));
        $(Selectors.byId("confirmCheck")).click();
        //submit transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();
        //Check submit alert and submit text
        Alert alert = switchTo().alert();
        //check alert content
        String alertText = alert.getText();
        assertThat(alertText.contains(String.valueOf(deposit)));
        assertThat(alertText.contains(createSecondAccountResponse.getAccountNumber()));
        alert.accept();
        //navigate to transfer history page and check that transfer present
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        $(Selectors.byText("\uD83D\uDD0D Search Transactions")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter name to find transactions")).sendKeys(secondUserRequest.getUsername());
        ElementsCollection transactionList = $(Selectors.byCssSelector("li.list-group-item")).findAll("span");
        boolean containsInUITransactionList = transactionList.stream().map(SelenideElement::getText).anyMatch(transactionText -> {
            return transactionText.contains(secondUserRequest.getUsername()) &&
                    transactionText.contains(TRANSFER_IN_TRANSACTION_TYPE) && transactionText.contains(String.valueOf(transferAmount))  ;
        });
        assertThat(containsInUITransactionList);
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
    @DisplayName("User can't transfer more then current deposit")
    public void userCantTransferMoreThanCurrentDepositTest() {
        double deposit = RandomData.getRandomDepositValue(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
        double transferAmount = RandomData.roundDoubleValue(deposit + 0.01, DEFAULT_DOUBLE_PRECISION);
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
                new CreateDepositRequest(firstAccountResponse.getId(), firstAccountResponse.getAccountNumber(), deposit)
        );

        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());
        //user login
        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.LOGIN
        )
                .post(new LoginUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .extract().header("Authorization");
        //open dashboard page
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //open transfer page
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).shouldBe(Condition.visible);
        //select account and amount money to deposit
        $(Selectors.byCssSelector(".account-selector")).selectOptionContainingText(firstAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(createUserRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountResponse.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transferAmount));
        $(Selectors.byId("confirmCheck")).click();
        //submit transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();
        //Check submit alert and submit text
        Alert alert = switchTo().alert();
        //check alert content
        String alertText = alert.getText();
        assertThat(alertText.equals("❌ Error: Invalid transfer: insufficient funds or invalid accounts"));

        //navigate to transfer history page and check that transfer present
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        $(Selectors.byText("\uD83D\uDD0D Search Transactions")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter name to find transactions")).sendKeys(createUserRequest.getUsername());
        //check tht UI doesn't contains transaction
        ElementsCollection transactionList = $(Selectors.byCssSelector("li.list-group-item")).findAll("span");
        boolean containsInUITransactionList = transactionList.stream().map(SelenideElement::getText).anyMatch(transactionText -> {
            return transactionText.contains(createUserRequest.getUsername()) &&
                    transactionText.contains(TRANSFER_IN_TRANSACTION_TYPE) && transactionText.contains(String.valueOf(transferAmount))  ;
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
