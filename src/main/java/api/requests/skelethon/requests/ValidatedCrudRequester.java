package api.requests.skelethon.requests;

import api.requests.skelethon.interfaces.GetAllEndPoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndPointInterface;

import java.util.Arrays;
import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndPointInterface, GetAllEndPoint {
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
    public List<T> getAll(Class<?> clazz) {
        T[] array =  (T[]) crudRequester.getAll(clazz)
                .extract()
                .as(clazz);
        return Arrays.asList(array);
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
