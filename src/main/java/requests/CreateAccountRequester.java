package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class CreateAccountRequester extends Request{

    public CreateAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse post(BaseModel baseModel) {
        return given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .spec(requestSpecification)
                .post("/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
