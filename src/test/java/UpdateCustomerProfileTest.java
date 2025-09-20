import generators.RandomData;
import generators.RandomModelGenerator;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.EndPoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import services.CustomerService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;


public class UpdateCustomerProfileTest extends BaseTest {

    @Test
    public void customerCanChangeHisUsernameTest() {
        //create user with randomly generated data
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        //update customer name
        String newCustomerName = RandomData.getUsername() + " " + RandomData.getUsername();
        new ValidatedCrudRequester<UpdateCustomerResponse>(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk(),
                EndPoint.UPDATE_CUSTOMER
        ).update(UpdateCustomerNameRequest.builder().name(newCustomerName).build());

        //check that customer name was updated
        CreateUserResponse customerWithUpdateName = CustomerService.getCustomers()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().equals(newCustomerName))
                .findFirst().orElseThrow(() -> new RuntimeException("Customer with updated name wasn't found"));
    }

    public static Stream<Arguments> userNameInvalidData() {
        return Stream.of(
                Arguments.of("Kate", "Name must contain two words with letters only"),
                Arguments.of(" ", "Name must contain two words with letters only"),
                Arguments.of("Kate P!! ", "Name must contain two words with letters only")
        );
    }

    @ParameterizedTest
    @MethodSource("userNameInvalidData")
    public void userCantUpdateUsernameWithInvalidDataTest(String newName, String errorMessage) {
        //generate user data
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        //create randomly generated user
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                EndPoint.ADMIN_USER
        ).post(createUserRequest);

        //update customer name with invalid data
        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage),
                EndPoint.UPDATE_CUSTOMER
        ).update(UpdateCustomerNameRequest.builder().name(newName).build());

        //check that username wasn't updated
        Assertions.assertNull(CustomerService.getCustomerById(createUserResponse.getId()).get().getName());
    }
}
