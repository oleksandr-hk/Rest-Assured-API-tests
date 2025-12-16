package api.requests.skelethon.requests;

import api.configs.Config;
import api.models.BaseModel;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndPointInterface;
import api.requests.skelethon.interfaces.ReadableAlInterface;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndPointInterface, ReadableAlInterface {

    private static final String API_VERSION = Config.getProperty("apiVersion");

    public CrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, EndPoint endPoint) {
        super(requestSpecification, responseSpecification, endPoint);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("Post request to " + endPoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_VERSION + endPoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("Get request to endpoint {endpoint} with id {id}")
    public ValidatableResponse get(long  id) {
        return given()
                .spec(requestSpecification)
                .get(API_VERSION + endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("Get request to endpoint {endpoint}")
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get(API_VERSION + endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("Get request to endpoint {endpoint} with id {id}")
    public ValidatableResponse getAllById(long id) {
        return given()
                .spec(requestSpecification)
                .pathParams("id", id)
                .get(API_VERSION + endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("Get all request to endpoint {endpoint}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .when()
                .spec(requestSpecification)
                .get(API_VERSION + endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("Put request to endpoint {endpoint} with body {model}")
    public ValidatableResponse update(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(API_VERSION + endPoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("Delete request to endpoint {endpoint} with id {id}")
    public ValidatableResponse delete(long id) {
        return given()
                .spec(requestSpecification)
                .delete(API_VERSION + endPoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}