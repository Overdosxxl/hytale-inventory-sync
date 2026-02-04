package com.yourname.inventorysync.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.yourname.inventorysync.config.DatabaseConfig;
import net.hytale.api.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLConnection {
    private HikariDataSource dataSource;
    private InventoryDAO inventoryDAO;
    private Logger logger;
    
    public MySQLConnection(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database);
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        this.dataSource = new HikariDataSource(hikariConfig);
        this.inventoryDAO = new InventoryDAO(this);
        this.logger = InventorySyncMod.getInstance().getLogger();
    }
    
    public boolean connect() {
        try (Connection conn = dataSource.getConnection()) {
            logger.info("MySQL Verbindung getestet: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            logger.error("MySQL Verbindungsfehler: " + e.getMessage());
            return false;
        }
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public InventoryDAO getInventoryDAO() {
        return inventoryDAO;
    }
    
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
