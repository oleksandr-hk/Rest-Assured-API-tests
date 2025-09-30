package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferPage extends BasePage<TransferPage> {
    //top navigation buttons
    private SelenideElement newTransferBtn = $(Selectors.byText("\uD83C\uDD95 New Transfer"));
    private SelenideElement transferAgainBtn = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
    //transfer UI
    private SelenideElement accountSelector = $(Selectors.byCssSelector(".account-selector"));
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement sendTransferBtn = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private SelenideElement confirmCheckbox = $(Selectors.byId("confirmCheck"));
    //transaction page UI
    private ElementsCollection transactionsList = $$(Selectors.byCssSelector("li.list-group-item span"));
    private SelenideElement searchTransactionInput = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
    private SelenideElement confirmSearchButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage transfer(String accountNumber, String recipientName, String recipientAccountNumber, double amount) {
        newTransferBtn.click();
        sendTransferBtn.should(Condition.visible);
        accountSelector.selectOptionContainingText(accountNumber);
        recipientNameInput.sendKeys(recipientName);
        recipientAccountInput.sendKeys(recipientAccountNumber);
        amountInput.sendKeys(String.valueOf(amount));
        confirmCheckbox.click();
        sendTransferBtn.click();
        return this;
    };

    public List<String> getTransactions() {
        return transactionsList.stream().map(SelenideElement::getText).toList();
    }

    public TransferPage openTransactionList() {
        transferAgainBtn.click();
        confirmSearchButton.shouldBe(Condition.visible);
        return this;
    }
}
