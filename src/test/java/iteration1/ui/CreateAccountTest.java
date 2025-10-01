package iteration1.ui;

import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUITest {

    @Test
    @UserSession
    @DisplayName("Authorized user can create an account. User dashboard opened. User create anew account. Positive alert. API return an account")
    public void userCanCreateAccountTest() {
        //user create account
        new UserDashboard().open().createNewAccount()
                .createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();

        new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED);

        assertThat(createdAccounts).hasSize(1);
        assertThat(createdAccounts.getFirst().getBalance()).isZero();

    }
}
