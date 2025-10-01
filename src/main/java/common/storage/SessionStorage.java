package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();;
    private LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();


    private SessionStorage() {};

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user: users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    };

    public static CreateUserRequest getUser(int index) {
        return  new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(index - 1);
    }

    public static CreateUserRequest getUser() {
        return  getUser(1);
    }

    public static UserSteps getSteps(int index) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(index - 1);
    }

    public static UserSteps getSteps() {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).getFirst();
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
    }

}
