package iteration1.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;

import java.util.Map;

import static com.codeborne.selenide.LocalStorageConditions.item;
import static com.codeborne.selenide.LocalStorageConditions.itemWithValue;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.localStorage;

public class LoginUserTest {

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
    @DisplayName("Admin can login. Admin panel opened. Token saved at local storage")
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).should(Condition.visible);
        localStorage().shouldHave(itemWithValue("authToken", "Basic YWRtaW46YWRtaW4="));
    }

    @Test
    @DisplayName("User can login. User panel opened. Token saved at local storage")
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).should(Condition.visible).shouldHave(Condition.text("Welcome, noname"));
        localStorage().shouldHave(item("authToken"));
    }
}
