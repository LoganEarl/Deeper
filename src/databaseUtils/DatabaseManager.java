package databaseUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    public static final String SAVE_DIRECTORY = System.getProperty("user.dir").replace("\\", "/") + "/data/";

    public static void createDirectories(){
        File f = new File(SAVE_DIRECTORY);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            f.mkdirs();
        }
    }

    public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:" + SAVE_DIRECTORY + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
