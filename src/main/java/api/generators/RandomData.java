package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static constants.Constants.DEFAULT_DOUBLE_PRECISION;

public class RandomData {

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3)  + "%$";
    }

    public static double getRandomDepositValue(double leftLimit, double rightLimit) {
        double randomValue = leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
        return roundDoubleValue(randomValue, DEFAULT_DOUBLE_PRECISION);
    }

    public static double roundDoubleValue(double doubleValue, int precision) {
        return BigDecimal.valueOf(doubleValue)
                .setScale(precision, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
