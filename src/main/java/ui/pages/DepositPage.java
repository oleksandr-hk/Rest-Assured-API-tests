package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {

    protected SelenideElement accountSelector = $(Selectors.byCssSelector(".account-selector"));
    protected SelenideElement amount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    protected SelenideElement submitBtn = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage depositAccount(String accountNumber, double deposit) {
        accountSelector.selectOptionContainingText(accountNumber);
        amount.sendKeys(String.valueOf(deposit));
        submitBtn.click();
        return this;
    }
}
