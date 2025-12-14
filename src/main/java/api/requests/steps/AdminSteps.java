package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;

import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createUser() {
        //generate user data
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        return StepLogger.log("Admin creates user" + userRequest.getUsername(), () -> {
            //create user with previously generated data
            new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.entityWasCreated(),
                    EndPoint.ADMIN_USER)
                    .post(userRequest);

            return userRequest;
        });
    }

    public static List<CreateUserResponse> getAllUsers() {
        return StepLogger.log("Admin gets all users", () -> {
            return new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.requestReturnsOk(),
                    EndPoint.ADMIN_USER
            ).getAll(CreateUserResponse[].class);
        });
    }
}
