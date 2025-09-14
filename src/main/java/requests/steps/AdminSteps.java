package requests.steps;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
}
