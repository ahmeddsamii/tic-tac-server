package DataAccessLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAL 
{
        private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;

    public DAL() throws SQLException {
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/Players", "root", "root");
    }

    public void insert(PlayerDTO pto) throws SQLException {
        String query = "INSERT INTO PlayerInfo (score, username, password, email, status, player_ip) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            pst = con.prepareStatement(query);
            pst.setInt(1, 0);
            pst.setString(2, pto.getUsername());
            pst.setString(3, pto.getPassword());
            pst.setString(4, pto.getEmail());
            pst.setString(6, pto.getIp());
            pst.setString(5, "Offline");

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources();
        }
    }

    public void updateIpAndStatusOnline(PlayerDTO pto) throws SQLException {
        String query = "UPDATE PlayerInfo SET status = 'online', player_ip = ? WHERE username = ?";

        try {
            pst = con.prepareStatement(query);
            pst.setString(2, pto.getUsername());
            pst.setString(1, pto.getIp());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources();
        }
    }

}
