package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createnewAccount =  $(Selectors.byText("➕ Create New Account"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createnewAccount.click();
        return this;
    }

    public String getWelcomeTextContent() {
        return welcomeText.getText();
    }
}
