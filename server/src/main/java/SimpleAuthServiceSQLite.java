import java.sql.*;

public class SimpleAuthServiceSQLite implements AuthServiceSQLite {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public SimpleAuthServiceSQLite() throws SQLException, ClassNotFoundException {
        setConnection();
        createDb();
        readDB();
    }

    @Override
    public void setConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ChatDB.s2db");
    }

    @Override
    public void createDb() throws SQLException {
        statement = connection.createStatement();
        statement.execute(
                "CREATE TABLE if not exists 'Users'" +
                        "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' text, 'pass' text, 'nickname' text);");
    }

    public void readDB() throws SQLException, ClassNotFoundException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String login = resultSet.getString("login");
            String pass = resultSet.getString("pass");
            String nickname = resultSet.getString("nickname");
            System.out.println(id + " " + login + " " + pass + " " + nickname);
        }
    }

    @Override
    public String GetNicknameByLogAndPass(String login, String password) throws SQLException, ClassNotFoundException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String loginDB = resultSet.getString("login");
            String pass = resultSet.getString("pass");
            String nickname = resultSet.getString("nickname");
            if (login.equals(loginDB) && password.equals(pass)) {
                return nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration (String login, String password, String nickname) throws SQLException, ClassNotFoundException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            String loginDB = resultSet.getString("login");
            String nicknameDB = resultSet.getString("nickname");
            if (login.equals(loginDB) || nickname.equals(nicknameDB)) {
                return false;
            }
        }
        statement.execute(String.format("INSERT INTO 'Users' ('login', 'pass', 'nickname') VALUES ('%s', '%s', '%s')", login, password, nickname));
        return true;
    }

    @Override
    public void closeDB() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }

    @Override
    public boolean changeNickName(String login, String newNickname, String pass) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            String loginDB = resultSet.getString("login");
            String passwordDB = resultSet.getString("pass");
            String nicknameDB = resultSet.getString("nickname");
            if (login.equals(loginDB) && pass.equals(passwordDB) && !newNickname.equals(nicknameDB)) {
                statement.execute(String.format("UPDATE Users SET nickname =  '%s' WHERE login = '%s'", newNickname, login));
                return true;
            }
        }
        return false;
    }
}
