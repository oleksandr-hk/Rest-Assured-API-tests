package iteration1.api;

import common.extensions.TimingExtensions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtensions.class)
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
