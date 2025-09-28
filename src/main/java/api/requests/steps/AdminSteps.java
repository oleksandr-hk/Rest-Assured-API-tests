package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createUser() {
        //generate user data
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        //create user with previously generated data
        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ADMIN_USER)
                .post(createUserRequest);

        return createUserRequest;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.ADMIN_USER
                ).getAll(CreateUserResponse[].class);
    }
}
