package ui;

import api.BaseTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AdminSteps;
import services.CustomerService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCustomerProfileTest extends BaseTest {
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
    @DisplayName("User can change his name")
    public void userCanChangeHisNameTest() {
        //update customer name
        String newCustomerName = RandomData.getUsername() + " " + RandomData.getUsername();
        //create user with randomly generated data
        CreateUserRequest user = AdminSteps.createUser();

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
        //open edit profile page
        $(Selectors.byCssSelector("span.user-name")).click();
        $(Selectors.byAttribute("placeholder","Enter new name")).shouldBe(Condition.visible);
        //fill in new username
        $(Selectors.byAttribute("placeholder","Enter new name")).type(newCustomerName);
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();
        //check alert content
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText.contains("✅ Name updated successfully!"));
        alert.accept();
        //navigate back to dashboard page and check that greeting title updated with new username
        Selenide.back();
        Selenide.
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).should(Condition.visible);
        //$(Selectors.byText("Welcome, " + newCustomerName + "!")).shouldBe(Condition.visible);

        //check that customer name was updated
        CreateUserResponse customerWithUpdateName = CustomerService.getCustomers()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().equals(newCustomerName))
                .findFirst().orElseThrow(() -> new RuntimeException("Customer with updated name wasn't found"));
    }


    @Test
    @DisplayName("User can't change his name with name with single word")
    public void userCantChangeHisNameWithSingleWordTest() {
        //update customer name
        String newCustomerName = RandomData.getUsername();
        //create user with randomly generated data
        CreateUserRequest user = AdminSteps.createUser();

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
        //open edit profile page
        $(Selectors.byCssSelector("span.user-name")).click();
        $(Selectors.byAttribute("placeholder","Enter new name")).shouldBe(Condition.visible);
        //fill in new username
        $(Selectors.byAttribute("placeholder","Enter new name")).type(newCustomerName);
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();
        //check alert content
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText.contains("Name must contain two words with letters only"));
        alert.accept();
        //navigate back to dashboard page and check that greeting title updated with new username
        Selenide.back();
        Selenide.
                $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).should(Condition.visible);
        $(Selectors.byText("Welcome, " + newCustomerName + "!")).shouldNot(Condition.exist);

        //check that customer name wasn't updated
        assertThat(CustomerService.getCustomers()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().equals(newCustomerName))
                .count()).isEqualTo(0);
    }

}
