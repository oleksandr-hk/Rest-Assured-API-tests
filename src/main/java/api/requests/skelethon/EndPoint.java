package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
