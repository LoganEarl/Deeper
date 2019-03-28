package database;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    public static final String SAVE_DIRECTORY = System.getProperty("user.dir").replace("\\", "/") + "/data/";

    public static void createDirectories() {
        File f = new File(SAVE_DIRECTORY);
        if (!f.exists()) {
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

    public static Connection getDatabaseConnection(String fileName) {
        String url = "jdbc:sqlite:" + SAVE_DIRECTORY + fileName;

        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            return null;
        }
    }

    public static void createTables(String fileName, List<DatabaseTable> tables) {
        String curTableName = "";
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(getConnectionURL(fileName));
            Statement stmt = conn.createStatement();

            for (DatabaseTable table : tables) {
                curTableName = table.getTableName();
                StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table.getTableName()).append(" (");

                Map<String, String> columnDefinitions = table.getColumnDefinitions();
                boolean first = true;
                for (String columnName : columnDefinitions.keySet()) {
                    if (!first)
                        sql.append(",");
                    else
                        first = false;
                    sql.append(columnName).append(" ").append(columnDefinitions.get(columnName));
                }
                if (table.getConstraints() != null)
                    for (String constraint : table.getConstraints())
                        sql.append(",").append(constraint);


                sql.append(")");
                stmt.executeUpdate(sql.toString());
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Failed to create tables, table: " + curTableName);
            e.printStackTrace();
        }
    }


    private static String getConnectionURL(String fileName) {
        return "jdbc:sqlite:" + SAVE_DIRECTORY + fileName;
    }

    public interface DatabaseTable {
        String getTableName();

        Map<String, String> getColumnDefinitions();

        List<String> getConstraints();
    }

    public static int executeStatement(String sql, String databaseName, Object... args) {
        try {
            Connection c = DatabaseManager.getDatabaseConnection(databaseName);
            if (c == null)
                return -1;
            PreparedStatement statement = c.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer)
                    statement.setInt(i + 1, (Integer) args[i]);
                else if (args[i] instanceof String)
                    statement.setString(i + 1, (String) args[i]);
            }

            int result = statement.executeUpdate();
            statement.close();
            c.close();
            return result;
        } catch (SQLException e) {
            return -1;
        }
    }

    public interface DatabaseEntry {
        boolean saveToDatabase(String databaseName);

        boolean removeFromDatabase(String databaseName);

        boolean updateInDatabase(String databaseName);

        boolean existsInDatabase(String databaseName);
    }
}
