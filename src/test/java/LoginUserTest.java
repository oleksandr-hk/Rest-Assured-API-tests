import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class LoginUserTest extends BaseTest {


    @Test
    public void adminCanGenerateAuthTokenTest() {
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body("""
                        {
                            "username": "admin",
                            "password": "admin"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        //let's create user firstly
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                            "username": "kate2000",
                            "password": "Kate2000#",
                            "role": "USER"                       
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);



        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .body("""
                        {
                            "username": "kate2000",
                            "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .header("Authorization", Matchers.notNullValue());
    }

}
