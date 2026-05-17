package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PrestigeListener implements Listener {
    private final KatanaModPlugin plugin;
    private final DataManager dataManager;
    private final BossBarManager bossBarManager;
    private final KatanaManager katanaManager;

    public PrestigeListener(KatanaModPlugin plugin, DataManager dataManager, BossBarManager bossBarManager, KatanaManager katanaManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.bossBarManager = bossBarManager;
        this.katanaManager = katanaManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        
        // Czyszczenie wiadomości o śmierci z formatowania (prefiks/sufiks)
        String deathMsg = event.getDeathMessage();
        if (deathMsg != null) {
            event.setDeathMessage(cleanMessage(victim, deathMsg));
        }

        Player killer = victim.getKiller();

        // Handle victim XP loss
        ItemStack victimItem = victim.getInventory().getItemInMainHand();
        String victimKatana = katanaManager.getKatanaType(victimItem);
        if (victimKatana != null) {
            int currentXP = dataManager.getXP(victim.getUniqueId(), victimKatana);
            WeaponXPManager.Rank rank = WeaponXPManager.getRank(currentXP);
            if (rank != WeaponXPManager.Rank.PRESTIGE) {
                int loss = (int) (currentXP * rank.getDeathLoss());
                dataManager.addXP(victim.getUniqueId(), victimKatana, -loss);
                victim.sendMessage(ChatColor.RED + "Straciłeś " + loss + " XP katanie " + victimKatana + " z powodu śmierci!");
                bossBarManager.updateBossBar(victim, victimKatana);
            }
        }

        // Handle killer XP gain
        if (killer != null) {
            ItemStack killerItem = killer.getInventory().getItemInMainHand();
            String killerKatana = katanaManager.getKatanaType(killerItem);
            if (killerKatana != null) {
                dataManager.addXP(killer.getUniqueId(), killerKatana, 10);
                killer.sendMessage(ChatColor.GREEN + "+10 XP dla Twojej katany!");
                bossBarManager.updateBossBar(killer, killerKatana);

                // Check for SAMURAI reward
                if (dataManager.hasAllPrestige(killer.getUniqueId())) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + killer.getName() + " parent addtemp samurai 30d");
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "GRATULACJE! Zdobyłeś prestiż na wszystkich katanach. Otrzymujesz rangę SAMURAI na 30 dni!");
                }
            }
        }
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        String msg = event.getQuitMessage();
        if (msg != null) {
            event.setQuitMessage(cleanMessage(event.getPlayer(), msg));
        }
    }

    @EventHandler
    public void onKick(org.bukkit.event.player.PlayerKickEvent event) {
        String msg = event.getLeaveMessage();
        if (msg != null) {
            event.setLeaveMessage(cleanMessage(event.getPlayer(), msg));
        }
    }

    private String cleanMessage(Player player, String message) {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = sb.getTeam("prestige_" + player.getName());
        if (team != null) {
            String prefix = team.getPrefix();
            String suffix = team.getSuffix();
            if (prefix != null && !prefix.isEmpty()) {
                message = message.replace(prefix, "");
            }
            if (suffix != null && !suffix.isEmpty()) {
                message = message.replace(suffix, "");
            }
        }
        return message;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        String katanaType = katanaManager.getKatanaType(item);
        bossBarManager.updateBossBar(player, katanaType);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String katanaType = katanaManager.getKatanaType(item);
        bossBarManager.updateBossBar(player, katanaType);
    }
}
