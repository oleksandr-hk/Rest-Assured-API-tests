package api.requests.skelethon.requests;

import api.models.BaseModel;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndPointInterface;
import api.requests.skelethon.interfaces.ReadableAlInterface;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndPointInterface, ReadableAlInterface {

    public CrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, EndPoint endPoint) {
        super(requestSpecification, responseSpecification, endPoint);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .post(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(long  id) {
        return given()
                .spec(requestSpecification)
                .get(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse getAllById(long id) {
        return given()
                .spec(requestSpecification)
                .pathParams("id", id)
                .get(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .when()
                .spec(requestSpecification)
                .get(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(long id) {
        return given()
                .spec(requestSpecification)
                .delete(endPoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}