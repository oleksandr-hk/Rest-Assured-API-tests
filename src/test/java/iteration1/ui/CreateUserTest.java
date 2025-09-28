package iteration1.ui;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comaprison.ModelAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserTest extends BaseUITest{

    @Test
    @DisplayName("Admin user can create new user with correct data. Positive alert. User added in user list on UI. API return all users")
    public void adminCanCreateUserTest() {
        //login as admin
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUther(admin);
        //create user
        //check alert content
        //check that user list contains newly created user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY)
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);
        //check API returns newly created user
        CreateUserResponse createdUser  = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser);
    }

    @Test
    @DisplayName("Admin can't create new user with incorrect data. Negative alert. User not added in user list on UI. API return all users without wrong one")
    public void adminCantCreateUserWithIncorrectDataTest() {
        //login as admin
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUther(admin);
        $(Selectors.byText("Add User")).shouldBe(Condition.visible);
        //create user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        //create user
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN3_AND_15_CHARACTERS)
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNot(Condition.exist);

        //check API returns newly created user
        long count = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();
        assertThat(count).isEqualTo(0);
    }
}
