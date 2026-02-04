package com.yourname.inventorysync.listener;

import net.hytale.api.entity.Player;
import net.hytale.api.event.entity.player.PlayerJoinEvent;
import net.hytale.api.event.entity.player.PlayerInventoryChangeEvent;
import net.hytale.api.event.SubscribeEvent;
import com.yourname.inventorysync.InventorySyncMod;
import com.yourname.inventorysync.config.ConfigManager;

public class PlayerInventoryListener {
    
    private final InventorySyncMod mod;
    
    public PlayerInventoryListener(InventorySyncMod mod) {
        this.mod = mod;
    }
    
    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Lade Inventar aus DB beim Join
        mod.getDatabase().getInventoryDAO().loadInventory(player.getUniqueId());
        
        // Setze Inventar nach kurzer Verzögerung (nach Spawn)
        player.getServer().getScheduler().runDelayed(() -> {
            byte[] inventoryData = mod.getDatabase().getInventoryDAO().loadInventory(player.getUniqueId());
            if (inventoryData != null) {
                player.getInventory().fromNbtBytes(inventoryData);
                player.sendMessage("§aInventar synchronisiert von anderem Server!");
            }
        }, 20L); // 1 Sekunde Verzögerung
    }
    
    @SubscribeEvent
    public void onInventoryChange(PlayerInventoryChangeEvent event) {
        Player player = event.getPlayer();
        
        // Speichere Inventar bei Änderung (debounced)
        player.getServer().getScheduler().runDelayed(() -> {
            mod.getDatabase().getInventoryDAO()
                .saveInventory(player, ConfigManager.getServerId());
        }, 100L); // 5 Sekunden Debounce
    }
}
