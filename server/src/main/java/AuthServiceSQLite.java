import java.sql.SQLException;

public interface AuthServiceSQLite {
    void setConnection() throws ClassNotFoundException, SQLException;
    void createDb() throws SQLException;
    String GetNicknameByLogAndPass(String login, String password) throws SQLException, ClassNotFoundException;
    boolean registration(String login, String password, String nickname) throws SQLException, ClassNotFoundException;
    void closeDB() throws SQLException;
    boolean changeNickName(String login, String newNickname, String pass) throws SQLException;
}
