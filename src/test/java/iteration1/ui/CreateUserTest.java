package iteration1.ui;

import api.requests.steps.AdminSteps;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comaprison.ModelAssertions;
import common.annotations.AdminSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUITest{

    @Test
    @AdminSession
    @DisplayName("Admin user can create new user with correct data. Positive alert. User added in user list on UI. API return all users")
    public void adminCanCreateUserTest() {
        //create user
        //check alert content
        //check that user list contains newly created user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .stream()
                .anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));
        //check API returns newly created user
        CreateUserResponse createdUser  = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser);
    }

    @Test
    @AdminSession
    @DisplayName("Admin can't create new user with incorrect data. Negative alert. User not added in user list on UI. API return all users without wrong one")
    public void adminCantCreateUserWithIncorrectDataTest() {
        //create user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        //create user
        assertFalse(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN3_AND_15_CHARACTERS.getMessage())
                .getAllUsers()
                .stream()
                .anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        //check API returns newly created user
        long count = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();
        assertThat(count).isEqualTo(0);
    }
}
