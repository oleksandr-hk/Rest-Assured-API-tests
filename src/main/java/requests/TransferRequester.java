package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferRequest;

import static io.restassured.RestAssured.given;

public class TransferRequester extends Request<TransferRequest> {
    public TransferRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse post(TransferRequest model) {
        return given()
                .spec(requestSpecification)
                .when()
                .body(model)
                .post("/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
