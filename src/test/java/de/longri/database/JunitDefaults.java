package de.longri.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public abstract class JunitDefaults {

    static ArrayList<Host> testHosts = new ArrayList<>();
    static String databaseName = "ClusterTestDB";
    //    static String databaseName = "TestDB2";
    static Host HOST1 = new Host("localhost", "13306");
    static Host HOST2 = new Host("localhost", "23306");
    static Host HOST3 = new Host("localhost", "33306");


    static DatabaseUser user = new SimpleDatabaseUser("admin", "admin-pw");
    static MariaDB_Cluster_Connection ClusterConnection;

    @BeforeAll
    static void setUp() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException {
        testHosts.add(HOST1);
        testHosts.add(HOST2);
        testHosts.add(HOST3);

        //before create a connection create the Database on Cluster
        createTestDB(databaseName);

        ClusterConnection = new MariaDB_Cluster_Connection(databaseName, user, testHosts);
    }

    @AfterAll
    static void tearDown() {
    }

    public static void createTestDB(String dataBaseName) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException {
        if (dBExists(dataBaseName)) {
            deleteTestDB(dataBaseName);
        }
        Abstract_Database database = new Abstract_Database(new MySqlConnection(HOST1.host, HOST1.port, dataBaseName, user)) {
            @Override
            public int getLastDatabaseSchemeVersion() {
                return 0;
            }

            @Override
            public int updateSchemeVersion(int oldDatabaseSchemeVersion, int... newDatabaseSchemeVersion) throws SQLException {
                return 0;
            }

            @Override
            public String getSqlDump() throws SQLException {
                return "";
            }

            @Override
            public String getMySqlDump() throws SQLException {
                return "";
            }
        };
        database.createNewDatabase();
    }

    public static void deleteTestDB(String dataBaseName) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException {
        Abstract_Database db = new Abstract_Database(new MySqlConnection(HOST1.host, HOST1.port, dataBaseName, user)) {
            @Override
            public String getString(String uniqueID, String s) {
                return null;
            }

            @Override
            public int getLastDatabaseSchemeVersion() {
                return 0;
            }

            @Override
            public int updateSchemeVersion(int i, int... ints) throws SQLException {
                return 0;
            }

            @Override
            public String getSqlDump() throws SQLException {
                return null;
            }

            @Override
            public String getMySqlDump() throws SQLException {
                return null;
            }
        };

        String sql = "DROP SCHEMA `" + dataBaseName + "`;";
        db.connect("UNIQUE_ID_DELETE_DB");
        db.createStatement().execute(sql);
        db.disconnect("UNIQUE_ID_DELETE_DB");
    }

    public static boolean dBExists(String dbName) throws ClassNotFoundException {

        try {

            Connection conn = getNativeConnection(dbName);

            ResultSet resultSet = conn.getMetaData().getCatalogs();
            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                if (databaseName.equals(dbName)) {
                    return true;
                }
            }
            resultSet.close();
        } catch (Exception ignore) {

        }
        return false;
    }

    private static Connection getNativeConnection(String dbName) throws ClassNotFoundException, GeneralSecurityException, IOException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");


        String connectionstring = "jdbc:mysql://" + HOST1.toString() + "/" + dbName;

        Properties properties = new Properties();
        properties.setProperty("user", user.getUserName());
        properties.setProperty("password", user.getUserPasswordDecrypted());
        properties.setProperty("MaxPooledStatements", "250");
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf-8");


        Connection conn = DriverManager.getConnection(connectionstring, properties);
        return conn;
    }

}
