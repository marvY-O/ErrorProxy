package Database;
import java.sql.*;

public class dbUsers {
	public static Boolean verify(String username, String password) {
		Connection conn = null;
		Boolean ans = false;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");

            String sql = "SELECT password FROM users WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String passwordFromDb = rs.getString("password");
                if (passwordFromDb.equals(password)) {
                    ans = true;
                } else {
                    ans = false;
                }
            } else {
                ans = false;
            }

            // Close resources
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection to SQLite database failed.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing SQLite database connection.");
                e.printStackTrace();
            }
        }
    	return ans;
    }
	public static boolean addEntry(String sender, String receiver, int bytes ) {
		Connection conn = null;
		Boolean ans = false;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:log.db");

            String sql = "INSERT INTO log (Timestamp, Sender, Receiver, Bytes) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
//            long time = System.currentTimeMillis() / 1000L;
            stmt.setString(1, Long.toString(System.currentTimeMillis() / 1000L));
            stmt.setString(2, sender);
            stmt.setString(3, receiver);
            stmt.setString(4, Integer.toString(bytes));
            
            int rs = stmt.executeUpdate();

            if (rs > 0) {
                ans = true;
            } else {
                ans = false;
            }

            // Close resources
            
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection to SQLite database failed.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing SQLite database connection.");
                e.printStackTrace();
            }
        }
		return ans;
	}

    public static Boolean banned(String IP){
        Connection conn = null;
		Boolean ans = false;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:banned.db");

            String sql = "SELECT IP FROM banned WHERE IP = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, IP);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String passwordFromDb = rs.getString("IP");
                if (passwordFromDb.equals(IP)) {
                    ans = true;
                } else {
                    ans = false;
                }
            } else {
                ans = false;
            }

            // Close resources
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection to SQLite database failed.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing SQLite database connection.");
                e.printStackTrace();
            }
        }
    	return ans;
    }
	
	public static void main(String args[]) {
		addEntry("192.168.1.4", "192.168.1.4", 2000);
	}
}

