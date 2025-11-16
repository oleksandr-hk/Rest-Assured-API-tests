package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class TimingExtensions implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private Map<String, Long> startTimes = new HashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
        startTimes.put(testName, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
        System.out.println("Thread  " + Thread.currentThread().getName() + ": Test started " + testName);
        Long testDuration = System.currentTimeMillis() - startTimes.get(testName);
        System.out.println("Thread  " + Thread.currentThread().getName() + ": Test duration " + testDuration);
    }

}
