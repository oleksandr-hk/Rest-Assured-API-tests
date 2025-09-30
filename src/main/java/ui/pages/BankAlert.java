package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NOT_VALID_AMOUNT("❌ Please enter a valid amount."),
    NOT_VALID_TRANSFER_AMOUNT("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $%s to account %s!"),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $%s to account %s!"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NOT_VALID_NAME("Name must contain two words with letters only");


    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
