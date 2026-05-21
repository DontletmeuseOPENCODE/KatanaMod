package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.UUID;

public class ManaManager {
    private final JavaPlugin plugin;
    private final KatanaManager katanaManager;
    private final HashMap<UUID, Double> playerMana = new HashMap<>();
    
    public static final double MAX_MANA = 100.0;
    public static final double BASE_REGEN_PER_SEC = 15.0;
    
    private final NamespacedKey meditationKey;
    private final NamespacedKey windBladeKey;

    public ManaManager(JavaPlugin plugin, KatanaManager katanaManager) {
        this.plugin = plugin;
        this.katanaManager = katanaManager;
        this.meditationKey = new NamespacedKey(plugin, "enchant_meditation");
        this.windBladeKey = new NamespacedKey(plugin, "enchant_windblade");
        
        startRegenTask();
    }

    public double getMana(UUID playerId) {
        return playerMana.getOrDefault(playerId, MAX_MANA);
    }

    public void setMana(UUID playerId, double amount) {
        playerMana.put(playerId, Math.max(0.0, Math.min(MAX_MANA, amount)));
    }

    public void addMana(UUID playerId, double amount) {
        double current = getMana(playerId);
        setMana(playerId, current + amount);
    }

    public void clearMana() {
        playerMana.clear();
    }

    public boolean consumeMana(Player player, double baseAmount) {
        UUID playerId = player.getUniqueId();
        double current = getMana(playerId);
        
        // Calculate mana cost reduction from Wind Blade enchantment
        double multiplier = 1.0;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand != null && hand.hasItemMeta()) {
            ItemMeta meta = hand.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(windBladeKey, PersistentDataType.INTEGER)) {
                int level = meta.getPersistentDataContainer().get(windBladeKey, PersistentDataType.INTEGER);
                if (level == 1) {
                    multiplier = 0.85; // 15% reduction
                } else if (level >= 2) {
                    multiplier = 0.70; // 30% reduction
                }
            }
        }
        
        double actualAmount = baseAmount * multiplier;
        
        if (current >= actualAmount) {
            setMana(playerId, current - actualAmount);
            return true;
        }
        return false;
    }

    private void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    
                    boolean holdingKatana = false;
                    double regenMultiplier = 1.0;
                    
                    if (hand != null && hand.hasItemMeta()) {
                        String type = katanaManager.getKatanaType(hand);
                        if (type != null) {
                            holdingKatana = true;
                            // Check for Meditation enchantment
                            ItemMeta meta = hand.getItemMeta();
                            if (meta != null && meta.getPersistentDataContainer().has(meditationKey, PersistentDataType.INTEGER)) {
                                int level = meta.getPersistentDataContainer().get(meditationKey, PersistentDataType.INTEGER);
                                if (level == 1) {
                                    regenMultiplier = 1.25;
                                } else if (level == 2) {
                                    regenMultiplier = 1.50;
                                } else if (level >= 3) {
                                    regenMultiplier = 1.75;
                                }
                            }
                        }
                    }
                    
                    double regenPerTick = (BASE_REGEN_PER_SEC * regenMultiplier) / 5.0; // 4 ticks = 5 times per second
                    double current = getMana(playerId);
                    
                    if (current < MAX_MANA) {
                        current = Math.min(MAX_MANA, current + regenPerTick);
                        setMana(playerId, current);
                    }
                    
                    if (holdingKatana) {
                        displayManaBar(player, current, BASE_REGEN_PER_SEC * regenMultiplier);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 4L); // Runs every 4 ticks (0.2s)
    }

    private void displayManaBar(Player player, double currentMana, double regenRate) {
        int totalBars = 10;
        int filledBars = (int) Math.round((currentMana / MAX_MANA) * totalBars);
        
        ChatColor barColor;
        if (currentMana > 70) {
            barColor = ChatColor.GREEN;
        } else if (currentMana > 30) {
            barColor = ChatColor.YELLOW;
        } else {
            barColor = ChatColor.RED;
        }
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.AQUA).append("Mana: ").append(barColor).append("⚡ ");
        
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("▮");
            } else {
                bar.append(ChatColor.GRAY).append("▯").append(barColor);
            }
        }
        
        bar.append(" ").append(ChatColor.WHITE).append((int) currentMana).append("/").append((int) MAX_MANA);
        bar.append(" ").append(ChatColor.GRAY).append("(+").append(String.format("%.1f", regenRate)).append("/s)");
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar.toString()));
    }
}
