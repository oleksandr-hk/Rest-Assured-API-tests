import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferTest extends BaseTest{

    @Test
    public void userCanTransferMoneyOnHisOwnAccountTest() {
        final double deposit = 100.0;
        //create user with randomly generated data
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create first account for user
        CreateAccountResponse firstAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //create second account for user
        CreateAccountResponse secondAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on first created account
        CreateAccountResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(firstAccount.getId())
                .accountNumber(firstAccount.getAccountNumber())
                .balance(deposit)
                .build()
        ).extract().as(CreateAccountResponse.class);

        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        //transfer money from first account to second one
        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(new TransferRequest(
                firstAccount.getId(),
                secondAccount.getId(),
                deposit
        )).extract().as(TransferResponse.class);

        //check second account balance
        softly.assertThat(deposit).isEqualTo(transferResponse.getAmount());
        //check that first account balance equal to 0
        softly.assertThat(0.0).isEqualTo( firstAccount.getBalance());
        //checks accounts id from transfer response
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(firstAccount.getId());
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(secondAccount.getId());
    }

    @Test
    public void userCanTransferMoneyOnAnotherUserAccountTest() {
        final double deposit = 100.0;
        //create user with randomly generated data
        CreateUserRequest firstUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create user with randomly generated data
        CreateUserRequest secondUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated first user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(firstUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create randomly generated second user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(secondUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create account for first user
        CreateAccountResponse firstUserAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //create account for second user
        CreateAccountResponse secondUserAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(secondUserRequest.getUsername(), secondUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on first user account
        CreateAccountResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(firstUserAccount.getId())
                .accountNumber(firstUserAccount.getAccountNumber())
                .balance(deposit)
                .build()
        ).extract()
                .as(CreateAccountResponse.class);
        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(TransferRequest.builder()
                .senderAccountId(firstUserAccount.getId())
                .receiverAccountId(secondUserAccount.getId())
                .amount(deposit).build())
                .extract().as(TransferResponse.class);

        //check transfer response
        softly.assertThat(deposit).isEqualTo(transferResponse.getAmount());
        softly.assertThat(secondUserAccount.getId()).isEqualTo(transferResponse.getReceiverAccountId());
        softly.assertThat(firstUserAccount.getId()).isEqualTo(transferResponse.getSenderAccountId());
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(10.0,  0.0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10.0, -1.0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10.0, 11.0, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCantTransferWrongAmountOfMoneyTest(double amountOfMoneyToDeposit, double amountMoneyToTransfer, String errorMessage) {
        //create user with randomly generated data
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //create randomly generated user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        //create first account for user
        CreateAccountResponse firstAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //create second account for user
        CreateAccountResponse secondAccount = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on first created account
        CreateAccountResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(firstAccount.getId())
                .accountNumber(firstAccount.getAccountNumber())
                .balance(amountOfMoneyToDeposit)
                .build()
        ).extract().as(CreateAccountResponse.class);

        //check that user balance equal to new one
        softly.assertThat(amountOfMoneyToDeposit).isEqualTo(depositResponse.getBalance());

        //try to transfer wrong amount of money between accounts
        new TransferRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadResponse(errorMessage)
        ).post(new TransferRequest(
                firstAccount.getId(),
                secondAccount.getId(),
                amountMoneyToTransfer
        ));
    }

}
