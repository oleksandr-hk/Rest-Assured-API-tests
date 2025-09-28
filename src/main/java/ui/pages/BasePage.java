package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import static org.assertj.core.api.Assertions.assertThat;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

public abstract class BasePage<T extends BasePage> {

    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement button = $(Selectors.byCssSelector("button"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    };

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(BankAlert bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert.getMessage());
        alert.accept();
        return (T) this;
    }

}
