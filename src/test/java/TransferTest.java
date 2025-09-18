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
import services.CustomerService;
import services.TransactionService;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferTest extends BaseTest{

    @Test
    public void userCanTransferMoneyOnHisOwnAccountTest() {
        double deposit = 25.0;
        double transferAmount = 10.0;
        double leftFirstOnAccount = 15.0;
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
        CreateAccountResponse firstAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //create second account for user
        CreateAccountResponse secondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on first created account
        CreateAccountResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(firstAccountResponse.getId())
                .accountNumber(firstAccountResponse.getAccountNumber())
                .balance(deposit)
                .build()
        ).extract().as(CreateAccountResponse.class);

        //check that user balance equal to new one
        softly.assertThat(deposit).isEqualTo(depositResponse.getBalance());

        //transfer money from first account to second one
        new TransferRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(new TransferRequest(
                firstAccountResponse.getId(),
                secondAccountResponse.getId(),
                transferAmount
        ));

        //check first account balance
        softly.assertThat(leftFirstOnAccount).isEqualTo(
                        CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        firstAccountResponse.getId()
                ).get()
                .getBalance());

        //check first account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                firstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == secondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        })).isEqualTo(true);
        //check second account balance
        softly.assertThat(transferAmount).isEqualTo(CustomerService.getCustomerAccountById(createUserRequest.getUsername(), createUserRequest.getPassword(), secondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                secondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == firstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        })).isEqualTo(true);
    }

    @Test
    public void userCanTransferMoneyOnAnotherUserAccountTest() {
        int deposit = 25;
        double transferAmount = 5.50;
        double leftOnFirstAccount = 19.50;
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
        CreateAccountResponse createFirstAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //create account for second user
        CreateAccountResponse createSecondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(secondUserRequest.getUsername(), secondUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(null).extract()
                .as(CreateAccountResponse.class);

        //deposit money on first user account
        new DepositRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(CreateDepositRequest.builder()
                .id(createFirstAccountResponse.getId())
                .accountNumber(createFirstAccountResponse.getAccountNumber())
                .balance(deposit)
                .build()
        );

        new TransferRequester(
                RequestSpecs.authAsUser(firstUserRequest.getUsername(), firstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()
        ).post(TransferRequest.builder()
                .senderAccountId(createFirstAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(transferAmount).build());

        //check first account balance
        softly.assertThat(leftOnFirstAccount).isEqualTo(CustomerService.getCustomerAccountById(
                        firstUserRequest.getUsername(),
                        firstUserRequest.getPassword(),
                        createFirstAccountResponse.getId())
                .get()
                .getBalance());

        //check first account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                firstUserRequest.getUsername(),
                firstUserRequest.getPassword(),
                createFirstAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createSecondAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        })).isEqualTo(true);
        //check second account balance
        softly.assertThat(transferAmount).isEqualTo(CustomerService.getCustomerAccountById(
                        secondUserRequest.getUsername(),
                        secondUserRequest.getPassword(),
                        createSecondAccountResponse.getId())
                .get()
                .getBalance());
        //check second account transactions list
        softly.assertThat(TransactionService.getAccountTransactions(
                secondUserRequest.getUsername(),
                secondUserRequest.getPassword(),
                createSecondAccountResponse.getId()
        ).stream().anyMatch(transaction -> {
            return  transaction.getRelatedAccountId() == createFirstAccountResponse.getId()
                    && transaction.getAmount() == transferAmount;
        })).isEqualTo(true);

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

        //check if second account balance = 0
        softly.assertThat(0.0).isEqualTo(CustomerService.getCustomerAccountById(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        secondAccount.getId()
                ).get()
                .getBalance());
    }

}
