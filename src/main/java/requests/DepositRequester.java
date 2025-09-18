package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateDepositRequest;

import static io.restassured.RestAssured.given;

public class DepositRequester extends Request<CreateDepositRequest> {

    public DepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateDepositRequest model) {
        return given()
                .spec(requestSpecification)
                .when()
                .body(model)
                .post("/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
