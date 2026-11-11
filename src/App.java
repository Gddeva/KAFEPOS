import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        System.out.println("Mencoba mengakses database...");

        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            try {
                String sql = "SELECT * FROM kafe";

                PreparedStatement stmt = conn.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n=== KASIR_CAFE ===");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nama = rs.getString("nama");
                    String telepon = rs.getString("telepon");
                    
                    System.out.println("ID: " + id + " | Nama Kafe: " + nama + " | Telepon: " + telepon);
                }
                
                rs.close();
                stmt.close();
                conn.close();
                
            } catch (SQLException e) {
                System.out.println("Gagal membaca database: " + e.getMessage());
            }
        }
    }
}