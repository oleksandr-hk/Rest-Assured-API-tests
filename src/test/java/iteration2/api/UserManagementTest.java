package iteration2.api;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comaprison.ModelAssertions;
import api.requests.skelethon.EndPoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class UserManagementTest  extends BaseTest {

    @Test
    public void adminCanDeleteUserById() {
        //generate a new user
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        //create a new user
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        ResponseSpecs.entityWasCreated(),
                        EndPoint.ADMIN_USER
                )
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

        //delete newly created user
        new CrudRequester(
                        RequestSpecs.adminSpec(),
                        ResponseSpecs.requestReturnsOk(),
                        EndPoint.ADMIN_DELETE_USER
                ).delete(createUserResponse.getId());

        //Read all users
        List<CreateUserResponse> users =AdminSteps.getAllUsers();

        //check that user list doesn't contain deleted user
        Assertions.assertFalse(users.stream()
                .anyMatch(user -> user.getId() == createUserResponse.getId()));
    }

    @Test
    public void adminCantDeleteNonExistentUser() {
        final int NON_VALID_USER_ID = 0;
        //generate a new user
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        //create a new user
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        ResponseSpecs.entityWasCreated(),
                        EndPoint.ADMIN_USER
                )
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

        //Read all before delete attempt
        List<CreateUserResponse> usersBefore = AdminSteps.getAllUsers();

        //delete newly created user
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityNotFound(),
                EndPoint.ADMIN_DELETE_USER
        ).delete(NON_VALID_USER_ID);

        //Read all users after delete attempt
        List<CreateUserResponse> usersAfter = AdminSteps.getAllUsers();

        //check that user list didn't change
        Assertions.assertEquals(usersBefore.size(), usersAfter.size());
    }

}
