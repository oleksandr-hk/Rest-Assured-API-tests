package api.requests.skelethon.requests;

import api.models.BaseModel;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndPointInterface;
import api.requests.skelethon.interfaces.ReadableAlInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Arrays;
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
    public List<T> getAll(Class<?> clazz) {
        T[] array = (T[]) crudRequester.getAll(clazz)
                .extract()
                .as(clazz);
        return Arrays.asList(array);
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