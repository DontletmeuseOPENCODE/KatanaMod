package pl.szympixel.katanamod;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KatanaHitListener implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;

    public KatanaHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
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
                        // Trucizna na 2 sekundy (40 ticków), poziom 1 (amplifier 0)
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    }
                }
            }
        }
    }
}
