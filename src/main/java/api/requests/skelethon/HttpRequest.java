package api.requests.skelethon;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class HttpRequest {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;
    protected EndPoint endPoint;

    public HttpRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, EndPoint endPoint) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
        this.endPoint = endPoint;
    }
}
