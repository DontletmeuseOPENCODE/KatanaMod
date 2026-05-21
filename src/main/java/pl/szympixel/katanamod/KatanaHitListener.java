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
import org.bukkit.event.entity.EntityDeathEvent;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KatanaHitListener implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;
    private final NamespacedKey mergedEffectsKey;
    private final ManaManager manaManager;
    private final KatanaEnchantmentManager enchantmentManager;
    private final Set<UUID> dashingPlayers = new HashSet<>();

    public KatanaHitListener(JavaPlugin plugin, ManaManager manaManager, KatanaEnchantmentManager enchantmentManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.enchantmentManager = enchantmentManager;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
        this.mergedEffectsKey = new NamespacedKey(plugin, "merged_effects");
    }

    public void clearCooldowns() {
        manaManager.clearMana();
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
            event.getEnchanter().sendMessage(ChatColor.RED + "Nie można zaklinać katan w stole do zaklęć!");
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        
        // Zezwól na łączenie katan w naszym mergerze, ale zablokuj normalne kowadło
        // Jeśli to nie jest nasze GUI "Squash & Merge"
        if (event.getView().getTitle().equals("Squash & Merge")) {
            return;
        }
        
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

        // Kensai visual sweeping particle
        int kensaiLvl = enchantmentManager.getLevel(weapon, "kensai");
        if (kensaiLvl > 0) {
            player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, victim.getLocation().add(0, 1, 0), 1);
        }

        // Static Shock enchantment
        int staticShockLvl = enchantmentManager.getLevel(weapon, "staticshock");
        if (staticShockLvl > 0) {
            double chance = staticShockLvl == 1 ? 0.10 : 0.20;
            if (Math.random() < chance) {
                triggerStaticShock(player, victim);
            }
        }

        if ("wakizashi".equals(katanaType)) {
            if (manaManager.consumeMana(player, 20.0)) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            }
        } else if ("tanto".equals(katanaType)) {
            if (manaManager.consumeMana(player, 25.0)) {
                Location victimLoc = victim.getLocation();
                Vector direction = victimLoc.getDirection().normalize();
                Location behindLoc = victimLoc.clone().subtract(direction.multiply(1.5));
                behindLoc.setYaw(victimLoc.getYaw());
                behindLoc.setPitch(victimLoc.getPitch());
                player.teleport(behindLoc);
                player.sendMessage(ChatColor.AQUA + "Teleportacja!");
            } else {
                player.sendMessage(ChatColor.RED + "Za mało many! Teleportacja wymaga 25 many.");
            }
        } else if ("chisa".equals(katanaType)) {
            if (manaManager.consumeMana(player, 35.0)) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 9));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                player.sendMessage(ChatColor.RED + "Cień Chisa spowił twego wroga!");
            } else {
                player.sendMessage(ChatColor.RED + "Za mało many! Zamrożenie wymaga 35 many.");
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

    private void triggerStaticShock(Player player, LivingEntity victim) {
        Location victimLoc = victim.getLocation();
        victimLoc.getWorld().strikeLightningEffect(victimLoc);
        victim.damage(2.0, player);
        
        int chains = 0;
        for (org.bukkit.entity.Entity nearby : victim.getNearbyEntities(5, 5, 5)) {
            if (chains >= 3) break;
            if (nearby instanceof LivingEntity && nearby != player && nearby != victim) {
                LivingEntity target = (LivingEntity) nearby;
                Location targetLoc = target.getLocation();
                targetLoc.getWorld().strikeLightningEffect(targetLoc);
                target.damage(2.0, player);
                chains++;
            }
        }
        player.sendMessage(ChatColor.YELLOW + "✦ Wyładowanie łańcuchowe! ✦");
    }

    private void applyMergedHitEffect(String effect, Player player, LivingEntity victim) {
        switch (effect) {
            case "poison":
                if (manaManager.consumeMana(player, 20.0)) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                }
                break;
            case "teleport":
                if (manaManager.consumeMana(player, 25.0)) {
                    Location vLoc = victim.getLocation();
                    Vector dir = vLoc.getDirection().normalize();
                    Location behind = vLoc.clone().subtract(dir.multiply(1.5));
                    behind.setYaw(vLoc.getYaw());
                    behind.setPitch(vLoc.getPitch());
                    player.teleport(behind);
                    player.sendMessage(ChatColor.AQUA + "Teleportacja!");
                } else {
                    player.sendMessage(ChatColor.RED + "Za mało many na teleportację! (Wymagane: 25)");
                }
                break;
            case "freeze":
                if (manaManager.consumeMana(player, 35.0)) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 9));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                    player.sendMessage(ChatColor.RED + "Moc Chisy spowiła twego wroga!");
                } else {
                    player.sendMessage(ChatColor.RED + "Za mało many na zamrożenie! (Wymagane: 35)");
                }
                break;
            case "invisible":
                if (manaManager.consumeMana(player, 20.0)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
                }
                break;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (isKatana(weapon)) {
                int level = enchantmentManager.getLevel(weapon, "soulstealer");
                if (level > 0) {
                    double regen = level == 1 ? 10.0 : 20.0;
                    manaManager.addMana(killer.getUniqueId(), regen);
                    killer.sendMessage(ChatColor.DARK_PURPLE + "+" + (int)regen + " Many (Pożeracz Dusz)");
                }
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
        
        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!isKatana(weapon)) return;

        // Visual sweeping particle for Kensai swing
        if (action == org.bukkit.event.block.Action.LEFT_CLICK_AIR || action == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            int kensaiLvl = enchantmentManager.getLevel(weapon, "kensai");
            if (kensaiLvl > 0) {
                Location loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5));
                player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, loc, 1);
            }
            return;
        }

        if (action != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;

        String katanaType = meta.getPersistentDataContainer().get(katanaKey, PersistentDataType.STRING);
        UUID playerId = player.getUniqueId();

        if ("tachi".equals(katanaType)) {
            if (manaManager.consumeMana(player, 30.0)) {
                Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.8);
                dashVector.setY(0.4);
                player.setVelocity(dashVector);

                dashingPlayers.add(playerId);
                Bukkit.getScheduler().runTaskLater(plugin, () -> dashingPlayers.remove(playerId), 40L);

                player.sendMessage(ChatColor.YELLOW + "Dash!");
            } else {
                player.sendMessage(ChatColor.RED + "Za mało many! Dash wymaga 30 many.");
            }
        } else if ("smokebomb".equals(katanaType)) {
            if (manaManager.consumeMana(player, 40.0)) {
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
                        player.sendMessage(ChatColor.GRAY + "Twój kamuflaż wygasł.");
                    }
                }, 100L);

                // Zmniejsz ilość
                if (weapon.getAmount() > 1) {
                    weapon.setAmount(weapon.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }

                player.sendMessage(ChatColor.GRAY + "Rzut bombą dymną! Jesteś niewidzialny przez 5s.");
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Za mało many! Bomba dymna wymaga 40 many.");
            }
        } else if ("odachi".equals(katanaType)) {
            if (manaManager.consumeMana(player, 40.0)) {
                LivingEntity target = null;
                for (org.bukkit.entity.Entity nearby : player.getNearbyEntities(10, 5, 10)) {
                    if (nearby instanceof LivingEntity && nearby != player) {
                        target = (LivingEntity) nearby;
                        break;
                    }
                }

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Brak celu w zasięgu 10 bloków! Mana została zwrócona.");
                    manaManager.addMana(playerId, 40.0);
                    return;
                }

                Location loc = target.getLocation();
                loc.getBlock().getRelative(0, -1, 0).setType(Material.COBWEB);
                loc.getBlock().setType(Material.COBWEB);

                player.sendMessage(ChatColor.DARK_GREEN + "Pajęczyna zastawiona pod " + target.getName() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Za mało many! Pajęczyna wymaga 40 many.");
            }
        } else if ("merged".equals(katanaType)) {
            // Merged katana - efekty PPM
            String effects = meta.getPersistentDataContainer().get(mergedEffectsKey, PersistentDataType.STRING);
            if (effects != null) {
                for (String effect : effects.split(",")) {
                    applyMergedPPMEffect(effect, player);
                }
            }
        }
    }

    private void applyMergedPPMEffect(String effect, Player player) {
        UUID playerId = player.getUniqueId();
        switch (effect) {
            case "dash":
                if (manaManager.consumeMana(player, 30.0)) {
                    Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.8);
                    dashVector.setY(0.4);
                    player.setVelocity(dashVector);
                    dashingPlayers.add(playerId);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> dashingPlayers.remove(playerId), 40L);
                    player.sendMessage(ChatColor.YELLOW + "Dash!");
                } else {
                    player.sendMessage(ChatColor.RED + "Za mało many na Dash! (Wymagane: 30)");
                }
                break;
            case "cobweb":
                if (manaManager.consumeMana(player, 40.0)) {
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
                        player.sendMessage(ChatColor.DARK_GREEN + "Pajęczyna zastawiona pod " + target.getName() + "!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Brak celu w zasięgu 10 bloków! Mana została zwrócona.");
                        manaManager.addMana(playerId, 40.0);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Za mało many na Pajęczynę! (Wymagane: 40)");
                }
                break;
        }
    }
}
