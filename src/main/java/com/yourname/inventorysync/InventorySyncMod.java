package com.yourname.inventorysync;

import com.google.inject.Inject;
import net.hytale.api.HytaleMod;
import net.hytale.api.Server;
import net.hytale.api.config.ModConfig;
import net.hytale.api.event.EventManager;
import net.hytale.api.logging.Logger;
import com.yourname.inventorysync.database.MySQLConnection;
import com.yourname.inventorysync.listener.PlayerInventoryListener;

@ModConfig("inventory-sync")
public class InventorySyncMod extends HytaleMod {
    
    private static InventorySyncMod instance;
    private MySQLConnection dbConnection;
    
    @Inject
    public InventorySyncMod(Server server, Logger logger, EventManager eventManager) {
        super(server, logger, eventManager);
        instance = this;
    }
    
    @Override
    public void onInitialize() {
        logger.info("🚀 Initialisiere Inventory Sync Mod...");
        
        // Lade Konfiguration
        ConfigManager.init();
        
        // Starte MySQL Verbindung
        dbConnection = new MySQLConnection(ConfigManager.getDatabaseConfig());
        if (dbConnection.connect()) {
            logger.info("✅ MySQL Verbindung erfolgreich!");
        } else {
            logger.error("❌ MySQL Verbindung fehlgeschlagen!");
            return;
        }
        
        // Registriere Events
        registerEvents(new PlayerInventoryListener(this));
        
        // Erstelle notwendige Tabellen
        dbConnection.getInventoryDAO().initializeTables();
        
        logger.info("✅ Inventory Sync Mod vollständig geladen!");
    }
    
    @Override
    public void onShutdown() {
        if (dbConnection != null) {
            dbConnection.disconnect();
        }
    }
    
    public static InventorySyncMod getInstance() {
        return instance;
    }
    
    public MySQLConnection getDatabase() {
        return dbConnection;
    }
}
