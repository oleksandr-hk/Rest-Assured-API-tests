package iteration1.ui;

import com.codeborne.selenide.Condition;
import api.models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;


import static com.codeborne.selenide.LocalStorageConditions.item;
import static com.codeborne.selenide.LocalStorageConditions.itemWithValue;
import static com.codeborne.selenide.Selenide.localStorage;

public class LoginUserTest extends BaseUITest{

    @Test
    @DisplayName("Admin can login. Admin panel opened. Token saved at local storage")
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        new LoginPage().open().login(admin.getUsername(), admin.getPassword()).getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
        localStorage().shouldHave(itemWithValue("authToken", "Basic YWRtaW46YWRtaW4="));
    }

    @Test
    @DisplayName("User can login. User panel opened. Token saved at local storage")
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();
        new LoginPage().open().login(user.getUsername(), user.getPassword())
                        .getPage(UserDashboard.class).getWelcomeText()
                        .should(Condition.visible).shouldHave(Condition.text("Welcome, noname"));
        localStorage().shouldHave(item("authToken"));
    }
}
