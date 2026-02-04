package com.yourname.inventorysync.database;

import net.hytale.api.entity.Player;
import net.hytale.api.item.Inventory;
import net.hytale.api.logging.Logger;

import java.sql.*;
import java.util.UUID;

public class InventoryDAO {
    private final MySQLConnection connection;
    private final Logger logger;
    
    public InventoryDAO(MySQLConnection connection) {
        this.connection = connection;
        this.logger = InventorySyncMod.getInstance().getLogger();
    }
    
    public void initializeTables() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS player_inventory (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                player_uuid VARCHAR(36) NOT NULL UNIQUE,
                server_id VARCHAR(50) NOT NULL,
                inventory_data LONGBLOB NOT NULL,
                last_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_player_uuid (player_uuid),
                INDEX idx_server_id (server_id)
            )
            """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
            stmt.execute();
            logger.info("✅ Datenbanktabellen erstellt");
        } catch (SQLException e) {
            logger.error("Fehler beim Erstellen der Tabellen: " + e.getMessage());
        }
    }
    
    public void saveInventory(Player player, String serverId) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = player.getInventory();
        
        // Serialisiere Inventory zu Bytes (Hytale NBT Format)
        byte[] inventoryData = inventory.toNbtBytes();
        
        String sql = """
            INSERT INTO player_inventory (player_uuid, server_id, inventory_data) 
            VALUES (?, ?, ?) 
            ON DUPLICATE KEY UPDATE 
            inventory_data = VALUES(inventory_data), 
            server_id = VALUES(server_id), 
            last_sync = CURRENT_TIMESTAMP
            """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, serverId);
            stmt.setBytes(3, inventoryData);
            stmt.executeUpdate();
            
            logger.info("💾 Inventar gespeichert: " + player.getName() + " (" + serverId + ")");
            
        } catch (SQLException e) {
            logger.error("Fehler beim Speichern des Inventars: " + e.getMessage());
        }
    }
    
    public byte[] loadInventory(UUID playerUUID) {
        String sql = "SELECT inventory_data FROM player_inventory WHERE player_uuid = ? ORDER BY last_sync DESC LIMIT 1";
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] data = rs.getBytes("inventory_data");
                    logger.info("📥 Inventar geladen für UUID: " + playerUUID);
                    return data;
                }
            }
        } catch (SQLException e) {
            logger.error("Fehler beim Laden des Inventars: " + e.getMessage());
        }
        return null;
    }
}
