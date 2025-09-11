import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class CreateAccountTest {

    @Test
    public void userCanCreateAccountTest() {
        //let's create user firstly
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                            "username": "kate20001111",
                            "password": "Kate2000#!",
                            "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("kate20001111"))
                .body("password", Matchers.not(Matchers.equalTo("Kate2000#!")))
                .body("role", Matchers.equalTo("USER"));


        //extract header Authorization to variable
        String authToken = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body("""
                        {
                            "username": "kate20001111",
                            "password": "Kate2000#!"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .header("Authorization");

        //creating account for user
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }
}
