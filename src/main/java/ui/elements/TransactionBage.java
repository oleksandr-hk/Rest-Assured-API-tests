package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class TransactionBage extends BaseElement {
    private String transactionType;
    private double amount;

    public TransactionBage(SelenideElement element) {
        super(element);
        transactionType = element.getText().split(" - ")[0];
        String amountText = element.getText().split(" - ")[1].replace('$', ' ');
        amount = Double.parseDouble(amountText.substring(0, amountText.indexOf("\n")));
    }
}
