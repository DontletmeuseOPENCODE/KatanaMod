package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;
import org.bukkit.entity.Trident;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KatanaHitListener implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;
    private final HashMap<UUID, Long> tantoCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> tachiCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> odachiCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> chisaCooldowns = new HashMap<>();
    private final Set<UUID> dashingPlayers = new HashSet<>();

    public KatanaHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
    }

    public void clearCooldowns() {
        tantoCooldowns.clear();
        tachiCooldowns.clear();
        odachiCooldowns.clear();
        chisaCooldowns.clear();
        dashingPlayers.clear();
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

    // Usuwa fall damage po dashu Tachi
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        if (dashingPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (!weapon.hasItemMeta()) return;
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(katanaKey, PersistentDataType.STRING)) return;

        String katanaType = container.get(katanaKey, PersistentDataType.STRING);

        if ("wakizashi".equals(katanaType)) {
            // Trucizna na 5 sekund (100 ticków)
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));

        } else if ("tanto".equals(katanaType)) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (!tantoCooldowns.containsKey(playerId) || currentTime - tantoCooldowns.get(playerId) >= 5000) {
                Location victimLoc = victim.getLocation();
                Vector direction = victimLoc.getDirection().normalize();
                Location behindLoc = victimLoc.clone().subtract(direction.multiply(1.5));
                behindLoc.setYaw(victimLoc.getYaw());
                behindLoc.setPitch(victimLoc.getPitch());
                player.teleport(behindLoc);
                tantoCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.AQUA + "Teleportacja!");
            } else {
                long timeLeft = 5000 - (currentTime - tantoCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Teleportacja gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        } else if ("chisa".equals(katanaType)) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (!chisaCooldowns.containsKey(playerId) || currentTime - chisaCooldowns.get(playerId) >= 10000) {
                // Zamrożenie (Slow 10) na 5 sekund i Blindness na 10 sekund
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 9));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                chisaCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.RED + "Cień Chisa spowił twego wroga!");
            } else {
                long timeLeft = 10000 - (currentTime - chisaCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Moc Chisy gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident)) return;
        Trident trident = (Trident) event.getEntity();
        ItemStack item = trident.getItemStack();
        
        if (!isKatana(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String type = meta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);
        if (!"shuriken".equals(type)) return;

        if (event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) event.getHitEntity();
            victim.setFireTicks(20); // 1 sekunda ognia
            trident.remove(); // Znika po trafieniu
        } else if (event.getHitBlock() != null) {
            // Chybienie - teraz też znika, bo mamy reload
            trident.remove();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        org.bukkit.event.block.Action action = event.getAction();
        if (action != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!isKatana(weapon)) return;
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;

        String katanaType = meta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if ("tachi".equals(katanaType)) {
            if (!tachiCooldowns.containsKey(playerId) || currentTime - tachiCooldowns.get(playerId) >= 20000) {
                // Delikatniejszy dash (nerfed), ochrona przed fall damage 3 sek.
                Vector dashVector = player.getLocation().getDirection().normalize().multiply(0.9);
                dashVector.setY(0.2);
                player.setVelocity(dashVector);

                dashingPlayers.add(playerId);
                Bukkit.getScheduler().runTaskLater(plugin, () -> dashingPlayers.remove(playerId), 60L);

                tachiCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.YELLOW + "Dash!");
            } else {
                long timeLeft = 20000 - (currentTime - tachiCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Dash gotowy za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }

        } else if ("shuriken".equals(katanaType)) {
            event.setCancelled(true);
            
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey ammoKey = new NamespacedKey(plugin, "shuriken_ammo");
            int ammo = container.getOrDefault(ammoKey, PersistentDataType.INTEGER, 5);

            if (ammo > 0) {
                // Rzut shurikenem
                Trident shurikenEntity = player.launchProjectile(Trident.class);
                shurikenEntity.setItem(weapon.clone());
                shurikenEntity.setShooter(player);
                
                ammo--;
                container.set(ammoKey, PersistentDataType.INTEGER, ammo);
                weapon.setItemMeta(meta);
                weapon.setAmount(Math.max(1, ammo));

                if (ammo == 0) {
                    player.sendMessage(ChatColor.RED + "Shurikeny się skończyły! Przeładowywanie...");
                } else {
                    player.sendMessage(ChatColor.DARK_BLUE + "Rzut shurikenem! Zostało: " + ammo);
                }

                // Zadanie reloadu
                new org.bukkit.scheduler.BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            this.cancel();
                            return;
                        }
                        
                        ItemMeta m = weapon.getItemMeta();
                        if (m == null) {
                            this.cancel();
                            return;
                        }
                        int currentAmmo = m.getPersistentDataContainer().getOrDefault(ammoKey, PersistentDataType.INTEGER, 0);
                        
                        if (currentAmmo < 5) {
                            currentAmmo++;
                            m.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, currentAmmo);
                            weapon.setItemMeta(m);
                            weapon.setAmount(currentAmmo);
                            if (currentAmmo == 5) {
                                player.sendMessage(ChatColor.GREEN + "Shurikeny w pełni przeładowane!");
                                this.cancel();
                            }
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 40L, 40L); // Co 2 sekundy +1 shuriken

            } else {
                player.sendMessage(ChatColor.RED + "Poczekaj na przeładowanie!");
            }

        } else if ("odachi".equals(katanaType)) {
            if (!odachiCooldowns.containsKey(playerId) || currentTime - odachiCooldowns.get(playerId) >= 30000) {
                // Znajdź cel przed graczem w zasięgu 10 bloków
                LivingEntity target = null;
                for (org.bukkit.entity.Entity nearby : player.getNearbyEntities(10, 5, 10)) {
                    if (nearby instanceof LivingEntity && nearby != player) {
                        target = (LivingEntity) nearby;
                        break;
                    }
                }

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Brak celu w zasięgu 10 bloków!");
                    return;
                }

                // Połóż pajęczynę pod i wokół celu
                Location loc = target.getLocation();
                loc.getBlock().getRelative(0, -1, 0).setType(Material.COBWEB);
                loc.getBlock().setType(Material.COBWEB);

                odachiCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.DARK_GREEN + "Pajęczyna zastawiona pod " + target.getName() + "!");
            } else {
                long timeLeft = 30000 - (currentTime - odachiCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Umiejętność gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (current == null || cursor == null) return;
        if (current.getType() != Material.TRIDENT || cursor.getType() != Material.TRIDENT) return;

        if (isKatana(current) && isKatana(cursor)) {
            ItemMeta currentMeta = current.getItemMeta();
            ItemMeta cursorMeta = cursor.getItemMeta();
            
            String currentType = currentMeta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);
            String cursorType = cursorMeta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);

            if ("shuriken".equals(currentType) && "shuriken".equals(cursorType)) {
                if (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE) {
                    int total = current.getAmount() + cursor.getAmount();
                    if (total <= 16) {
                        current.setAmount(total);
                        event.setCursor(null);
                        event.setCancelled(true);
                    } else {
                        current.setAmount(16);
                        cursor.setAmount(total - 16);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
