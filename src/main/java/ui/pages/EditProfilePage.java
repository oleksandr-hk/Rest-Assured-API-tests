package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class EditProfilePage extends BasePage<EditProfilePage> {

    private SelenideElement nameInput = $(Selectors.byAttribute("placeholder","Enter new name"));
    private SelenideElement saveButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));


    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage updateName(String newName) {
        nameInput.type(newName);
        saveButton.click();
        return this;
    };

}
