package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@AllArgsConstructor
@Getter
public enum EndPoint {

    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    ACCOUNT_TRANSACTIONS(
            "/accounts/{id}/transactions",
            BaseModel.class,
            TransactionResponse.class
    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    DEPOSIT(
        "/accounts/deposit",
            CreateDepositRequest.class,
            CreateAccountResponse.class
    ),
    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),
    UPDATE_CUSTOMER(
            "/customer/profile",
            UpdateCustomerNameRequest.class,
            UpdateCustomerResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
