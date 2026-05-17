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
    private final NamespacedKey mergedEffectsKey;
    private final HashMap<UUID, Long> tantoCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> tachiCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> odachiCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> chisaCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> smokeBombCooldowns = new HashMap<>();
    private final Set<UUID> dashingPlayers = new HashSet<>();

    public KatanaHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
        this.mergedEffectsKey = new NamespacedKey(plugin, "merged_effects");
    }

    public void clearCooldowns() {
        tantoCooldowns.clear();
        tachiCooldowns.clear();
        odachiCooldowns.clear();
        chisaCooldowns.clear();
        smokeBombCooldowns.clear();
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
            if (!chisaCooldowns.containsKey(playerId) || currentTime - chisaCooldowns.get(playerId) >= 20000) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 9));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                chisaCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.RED + "Cień Chisa spowił twego wroga!");
            } else {
                long timeLeft = 20000 - (currentTime - chisaCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Moc Chisy gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        } else if ("merged".equals(katanaType)) {
            // Merged katana - sprawdź efekty uderzeniowe
            String effects = container.get(mergedEffectsKey, PersistentDataType.STRING);
            if (effects != null) {
                for (String effect : effects.split(",")) {
                    applyMergedHitEffect(effect, player, victim);
                }
            }
        }
    }

    private void applyMergedHitEffect(String effect, Player player, LivingEntity victim) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        switch (effect) {
            case "poison":
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                break;
            case "teleport":
                if (!tantoCooldowns.containsKey(playerId) || currentTime - tantoCooldowns.get(playerId) >= 5000) {
                    Location vLoc = victim.getLocation();
                    Vector dir = vLoc.getDirection().normalize();
                    Location behind = vLoc.clone().subtract(dir.multiply(1.5));
                    behind.setYaw(vLoc.getYaw());
                    behind.setPitch(vLoc.getPitch());
                    player.teleport(behind);
                    tantoCooldowns.put(playerId, currentTime);
                }
                break;
            case "freeze":
                if (!chisaCooldowns.containsKey(playerId) || currentTime - chisaCooldowns.get(playerId) >= 20000) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 9));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                    chisaCooldowns.put(playerId, currentTime);
                }
                break;
            case "invisible":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        ItemStack item = null;
        if (event.getEntity() instanceof org.bukkit.entity.ThrownPotion) {
            item = ((org.bukkit.entity.ThrownPotion) event.getEntity()).getItem();
        } else if (event.getEntity() instanceof Trident) {
            item = ((Trident) event.getEntity()).getItemStack();
        }

        if (item == null || !isKatana(item)) return;
        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);

        if ("smokebomb".equals(type)) {
            Location loc = event.getEntity().getLocation();
            new org.bukkit.scheduler.BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 200) {
                        this.cancel();
                        return;
                    }
                    for (int i = 0; i < 8; i++) {
                        double x = (Math.random() - 0.5) * 5;
                        double y = (Math.random()) * 4;
                        double z = (Math.random() - 0.5) * 5;
                        Location pLoc = loc.clone().add(x, y, z);
                        loc.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.1, 0, 0.05);
                        
                        // Kolorowy kurz (Dust)
                        org.bukkit.Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(
                            org.bukkit.Color.fromRGB((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)), 1.5F);
                        loc.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, pLoc, 1, dust);
                    }
                    ticks += 5;
                }
            }.runTaskTimer(plugin, 0L, 5L);
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
            if (!tachiCooldowns.containsKey(playerId) || currentTime - tachiCooldowns.get(playerId) >= 5000) {
                Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.8);
                dashVector.setY(0.4);
                player.setVelocity(dashVector);

                dashingPlayers.add(playerId);
                Bukkit.getScheduler().runTaskLater(plugin, () -> dashingPlayers.remove(playerId), 40L);

                tachiCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.YELLOW + "Dash!");
            } else {
                long timeLeft = 5000 - (currentTime - tachiCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Dash gotowy za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        } else if ("smokebomb".equals(katanaType)) {
            if (!smokeBombCooldowns.containsKey(playerId) || currentTime - smokeBombCooldowns.get(playerId) >= 20000) {
                // Rzut bombą dymną
                event.setCancelled(true);
                org.bukkit.entity.ThrownPotion potion = player.launchProjectile(org.bukkit.entity.ThrownPotion.class);
                potion.setItem(weapon.clone());
                
                // Niewidzialność (5 sekund)
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
                
                // Ukrywanie zbroi
                ItemStack[] armor = player.getInventory().getArmorContents();
                player.getInventory().setArmorContents(new ItemStack[4]);
                
                // Przywracanie zbroi po 5 sekundach
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.getInventory().setArmorContents(armor);
                        player.sendMessage(ChatColor.GRAY + "Twoja kamuflaż wygasł.");
                    }
                }, 100L);

                // Zmniejsz ilość
                if (weapon.getAmount() > 1) {
                    weapon.setAmount(weapon.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }

                smokeBombCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.GRAY + "Rzut bombą dymną! Jesteś niewidzialny przez 5s.");
            } else {
                event.setCancelled(true);
                long timeLeft = 20000 - (currentTime - smokeBombCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Bomba dymna gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        } else if ("odachi".equals(katanaType)) {
            if (!odachiCooldowns.containsKey(playerId) || currentTime - odachiCooldowns.get(playerId) >= 30000) {
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

                Location loc = target.getLocation();
                loc.getBlock().getRelative(0, -1, 0).setType(Material.COBWEB);
                loc.getBlock().setType(Material.COBWEB);

                odachiCooldowns.put(playerId, currentTime);
                player.sendMessage(ChatColor.DARK_GREEN + "Pajęczyna zastawiona pod " + target.getName() + "!");
            } else {
                long timeLeft = 30000 - (currentTime - odachiCooldowns.get(playerId));
                player.sendMessage(ChatColor.RED + "Umiejętność gotowa za " + String.format("%.1f", timeLeft / 1000.0) + " s.");
            }
        } else if ("merged".equals(katanaType)) {
            // Merged katana - efekty PPM
            String effects = meta.getPersistentDataContainer().get(mergedEffectsKey, PersistentDataType.STRING);
            if (effects != null) {
                for (String effect : effects.split(",")) {
                    applyMergedPPMEffect(effect, player, playerId, currentTime);
                }
            }
        }
    }

    private void applyMergedPPMEffect(String effect, Player player, UUID playerId, long currentTime) {
        switch (effect) {
            case "dash":
                if (!tachiCooldowns.containsKey(playerId) || currentTime - tachiCooldowns.get(playerId) >= 5000) {
                    Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.8);
                    dashVector.setY(0.4);
                    player.setVelocity(dashVector);
                    dashingPlayers.add(playerId);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> dashingPlayers.remove(playerId), 40L);
                    tachiCooldowns.put(playerId, currentTime);
                    player.sendMessage(ChatColor.YELLOW + "Dash!");
                }
                break;
            case "cobweb":
                if (!odachiCooldowns.containsKey(playerId) || currentTime - odachiCooldowns.get(playerId) >= 30000) {
                    LivingEntity target = null;
                    for (org.bukkit.entity.Entity nearby : player.getNearbyEntities(10, 5, 10)) {
                        if (nearby instanceof LivingEntity && nearby != player) {
                            target = (LivingEntity) nearby;
                            break;
                        }
                    }
                    if (target != null) {
                        Location loc = target.getLocation();
                        loc.getBlock().getRelative(0, -1, 0).setType(Material.COBWEB);
                        loc.getBlock().setType(Material.COBWEB);
                        odachiCooldowns.put(playerId, currentTime);
                        player.sendMessage(ChatColor.DARK_GREEN + "Pajęczyna zastawiona pod " + target.getName() + "!");
                    }
                }
                break;
        }
    }
}
