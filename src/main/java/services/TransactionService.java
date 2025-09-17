package services;

import io.restassured.http.ContentType;
import models.Transaction;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

public class TransactionService {

    //get account transactions
    public static List<Transaction> getAccountTransactions(String username, String password, int accountId) {
        //generate basis auth token
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        return Arrays.asList(given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .pathParam("accountId", accountId)
                .header("Authorization", "Basic " + new String(encodedAuth))
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Transaction[].class));
    }
}
