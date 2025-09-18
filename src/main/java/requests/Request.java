package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request<T extends BaseModel> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public ValidatableResponse get() {
        throw new RuntimeException("Not implemented!");
    }

    public ValidatableResponse post(T model) {
        throw new RuntimeException("Not implemented!");
    }

    public ValidatableResponse put(T model) {
        throw new RuntimeException("Not implemented!");
    }
}
