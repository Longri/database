package de.longri.database;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;

public interface PoolConnectionCreater {
     PooledConnection createNewPooledConnection() throws SQLException, GeneralSecurityException, IOException;
}
