package iteration2.ui;

import api.generators.RandomData;
import common.annotations.UserSession;
import iteration1.ui.BaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.CustomerService;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;


import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCustomerProfileTest extends BaseUITest {

    @Test
    @UserSession
    @DisplayName("User can change his name")
    public void userCanChangeHisNameTest() {
        //update customer name
        String newCustomerName = RandomData.getUsername() + " " + RandomData.getUsername();
        //create user with randomly generated data

        new EditProfilePage().open().updateName(newCustomerName)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
        //navigate back to dashboard page and check that greeting title updated with new username
        assertThat(new UserDashboard().open().getWelcomeTextContent()).contains(newCustomerName);

        //check that customer name was updated
        CustomerService.getCustomers()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().equals(newCustomerName))
                .findFirst().orElseThrow(() -> new RuntimeException("Customer with updated name wasn't found"));
    }


    @Test
    @UserSession
    @DisplayName("User can't change his name with name with single word")
    public void userCantChangeHisNameWithSingleWordTest() {
        //update customer name
        String newCustomerName = RandomData.getUsername();

        new EditProfilePage().open().updateName(newCustomerName)
                .checkAlertMessageAndAccept(BankAlert.NOT_VALID_NAME.getMessage());

        //navigate back to dashboard page and check that greeting title updated with new username
        assertThat(new UserDashboard().open().getWelcomeTextContent()).doesNotContain(newCustomerName);

        assertThat(CustomerService.getCustomers()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().equals(newCustomerName))
                .count()).isEqualTo(0);
    }

}
