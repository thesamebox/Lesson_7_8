public interface AuthService {
    String GetNicknameByLogAndPass(String login, String password);

    boolean registration(String login, String password, String nickname);
}
