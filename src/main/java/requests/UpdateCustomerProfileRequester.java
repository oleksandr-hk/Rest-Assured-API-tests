package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UpdateCustomerNameRequest;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileRequester extends Request<UpdateCustomerNameRequest> {

    public UpdateCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse put(UpdateCustomerNameRequest model) {
        return given()
                .spec(requestSpecification)
                .when()
                .body(model)
                .put("/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

}
