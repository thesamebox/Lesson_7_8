import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{

    private class UserData {
        String login;
        String password;
        String nickName;

        public UserData(String login, String password, String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("qwe", "qwe", "petr"));
        users.add(new UserData("qwer", "qwer", "Victr"));
        users.add(new UserData("asd", "asd", "Masyanya"));
        users.add(new UserData("asdf", "asdf", "Nafanya"));
        users.add(new UserData("log4", "1234", "rogalDorn"));
        for (int i = 0; i < 10; i++) {
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));

        }
    }

    @Override
    public String GetNicknameByLogAndPass(String login, String password) {
        for (UserData user : users) {
            if(user.login.equals(login) && user.password.equals(password)) {
                return user.nickName;
            }
        }
        return null;
    }
}
