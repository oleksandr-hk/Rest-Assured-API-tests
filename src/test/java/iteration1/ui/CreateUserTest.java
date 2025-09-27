package iteration1.ui;

import com.codeborne.selenide.*;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comaprison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import specs.RequestSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
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
    @DisplayName("Admin user can create new user with correct data. Positive alert. User added in user list on UI. API return all users")
    public void adminCanCreateUserTest() {
        //login as admin
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Add User")).shouldBe(Condition.visible);
        //create user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();
        //check alert content
        Alert alert = switchTo().alert();
        assertEquals( "✅ User created successfully!", alert.getText());
        alert.accept();
        //check that user list contains newly created user
        ElementsCollection allUserFromDashBoard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUserFromDashBoard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);
        //check API returns newly created user
        CreateUserResponse[] users = given()
                .when()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CreateUserResponse[].class);
        CreateUserResponse createdUser = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser);
    }

    @Test
    @DisplayName("Admin can't create new user with incorrect data. Negative alert. User not added in user list on UI. API return all users without wrong one")
    public void adminCantCreateUserWithIncorrectDataTest() {
        //login as admin
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Add User")).shouldBe(Condition.visible);
        //create user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();
        //check wrong alert content
        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("Username must be between 3 and 15 characters"));
        alert.accept();
        //check that user list contains newly created user
        ElementsCollection allUserFromDashBoard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUserFromDashBoard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);
        //check API returns newly created user
        CreateUserResponse[] users = given()
                .when()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CreateUserResponse[].class);
        long count = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();
        assertThat(count).isEqualTo(0);
    }
}
