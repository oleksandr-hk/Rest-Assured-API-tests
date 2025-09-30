package iteration1.ui;

import api.requests.steps.UserSteps;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUITest {

    @Test
    @DisplayName("Authorized user can create an account. User dashboard opened. User create anew account. Positive alert. API return an account")
    public void userCanCreateAccountTest() {
        //admin login
        //admin create user
        CreateUserRequest user = AdminSteps.createUser();
        authAsUther(user);

        //user create account
        new UserDashboard().open().createNewAccount()
                .createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        assertThat(createdAccounts).hasSize(1);
        assertThat(createdAccounts.getFirst().getBalance()).isZero();

    }
}
