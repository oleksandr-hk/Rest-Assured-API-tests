package requests.skelethon.requests;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.EndPoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndPointInterface;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndPointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, EndPoint endPoint) {
        super(requestSpecification, responseSpecification, endPoint);
        this.crudRequester = new CrudRequester(requestSpecification, responseSpecification, endPoint);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model)
                .extract()
                .as(endPoint.getResponseModel());
    }

    @Override
    public T get(long id) {
        return (T) crudRequester.get(id)
                .extract()
                .as(endPoint.getResponseModel());
    }

    @Override
    public T update(long id, BaseModel model) {
        return (T) crudRequester.update(id, model)
                .extract()
                .as(endPoint.getResponseModel());
    }

    @Override
    public T delete(long id) {
        return (T) crudRequester.delete(id)
                .extract()
                .as(endPoint.getResponseModel());
    }
}
