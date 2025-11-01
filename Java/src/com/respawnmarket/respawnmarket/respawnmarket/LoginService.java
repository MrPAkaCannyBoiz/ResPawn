package respawnmarket;

import java.sql.*;

public class LoginService {

    public boolean login(String usernameOrEmail, String password) {
        String sql = """
            SELECT * FROM reseller WHERE username = ? AND password = ?
            UNION
            SELECT * FROM customer WHERE email = ? AND password = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, password);
            ps.setString(3, usernameOrEmail);
            ps.setString(4, password);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
