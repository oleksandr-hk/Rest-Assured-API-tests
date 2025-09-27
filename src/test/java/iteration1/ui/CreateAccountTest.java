package iteration1.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
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
    @DisplayName("Authorized user can create an account. User dashboard opened. User create anew account. Positive alert. API return an account")
    public void userCanCreateAccountTest() {
        //admin login
        //admin create user
        CreateUserRequest user = AdminSteps.createUser();
        //user login
        String authToken = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    ResponseSpecs.requestReturnsOk(),
                    EndPoint.LOGIN
                )
                .post(new LoginUserRequest(user.getUsername(), user.getPassword()))
                .extract().header("Authorization");
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authToken);
        Selenide.open("/dashboard");
        //user create account
        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText.contains("✅ New Account Created! Account Number:"));
        alert.accept();
        //check that account was created via API
        String createdAccountNumber = alertText.substring(alertText.indexOf(":") + 1).trim();
        CreateAccountResponse[] existingUserAccount = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract()
                .as(CreateAccountResponse[].class);
        CreateAccountResponse createdAccount = Arrays.stream(existingUserAccount).filter(
                account -> account.getAccountNumber().equals(createdAccountNumber)
        ).findFirst().get();
        assertThat(createdAccountNumber).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();
        assertThat(existingUserAccount).hasSize(1);

    }
}
