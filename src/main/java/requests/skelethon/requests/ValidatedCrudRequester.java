package requests.skelethon.requests;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.EndPoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndPointInterface;
import requests.skelethon.interfaces.ReadableAlInterface;

import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndPointInterface, ReadableAlInterface {
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
    public List<T> get() {
        return (List<T>) crudRequester.get()
                .extract()
                .jsonPath()
                .getList("", endPoint.getResponseModel());
    }

    @Override
    public List<T> getAllById(long id) {
        return (List<T>) crudRequester.getAllById(id)
                .extract()
                .jsonPath()
                .getList("", endPoint.getResponseModel());
    }

    @Override
    public T update(BaseModel model) {
        return (T) crudRequester.update(model)
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
