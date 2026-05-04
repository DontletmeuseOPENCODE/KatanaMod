package pl.szympixel.katanamod;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class KatanaHitListener implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;
    private final HashMap<UUID, Long> tantoCooldowns = new HashMap<>();

    public KatanaHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
    }

    private boolean isKatana(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(katanaKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (isKatana(event.getItem())) {
            event.setCancelled(true);
            event.getEnchanter().sendMessage(ChatColor.RED + "Nie można zaklinać katan!");
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        if (isKatana(first) || isKatana(second)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Sprawdzamy czy uderzył gracz
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        // Sprawdzamy czy ofiara jest żywym bytem
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player player = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon.hasItemMeta()) {
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                // Sprawdzamy, czy przedmiot to jakakolwiek z naszych katan
                if (container.has(katanaKey, PersistentDataType.STRING)) {
                    String katanaType = container.get(katanaKey, PersistentDataType.STRING);
                    
                    if ("wakizashi".equals(katanaType)) {
                        // Trucizna na 5 sekund (100 ticków), poziom 1 (amplifier 0)
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                    } else if ("tanto".equals(katanaType)) {
                        UUID playerId = player.getUniqueId();
                        long currentTime = System.currentTimeMillis();
                        if (!tantoCooldowns.containsKey(playerId) || currentTime - tantoCooldowns.get(playerId) >= 5000) {
                            // Oblicz pozycję z tyłu celu (odwrotny kierunek patrzenia ofiary)
                            Location victimLoc = victim.getLocation();
                            Vector direction = victimLoc.getDirection().normalize();
                            // Teleportujemy 1 blok za plecy
                            Location behindLoc = victimLoc.clone().subtract(direction.multiply(1.5));
                            behindLoc.setYaw(victimLoc.getYaw());
                            behindLoc.setPitch(victimLoc.getPitch());

                            player.teleport(behindLoc);
                            tantoCooldowns.put(playerId, currentTime);
                            player.sendMessage(org.bukkit.ChatColor.AQUA + "Teleportacja!");
                        } else {
                            long timeLeft = 5000 - (currentTime - tantoCooldowns.get(playerId));
                            player.sendMessage(org.bukkit.ChatColor.RED + "Teleportacja będzie gotowa za " + (timeLeft / 1000.0) + " s.");
                        }
                    }
                }
            }
        }
    }
}
