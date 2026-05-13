package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class RainbowTask extends BukkitRunnable {
    private final KatanaModPlugin plugin;
    private final DataManager dataManager;
    private final KatanaManager katanaManager;
    private int colorIndex = 0;
    private final ChatColor[] colors = {
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE
    };

    public RainbowTask(KatanaModPlugin plugin, DataManager dataManager, KatanaManager katanaManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.katanaManager = katanaManager;
    }

    @Override
    public void run() {
        ChatColor color = colors[colorIndex];
        colorIndex = (colorIndex + 1) % colors.length;

        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            String katanaType = katanaManager.getKatanaType(item);
            
            if (katanaType != null) {
                int xp = dataManager.getXP(player.getUniqueId(), katanaType);
                if (WeaponXPManager.getRank(xp) == WeaponXPManager.Rank.PRESTIGE) {
                    int prestige = WeaponXPManager.getPrestigeLevel(xp);
                    String prestigeText = ChatColor.GRAY + "[" + color + prestige + " PRESTIŻY" + ChatColor.GRAY + "] ";
                    
                    updatePlayerNametag(player, prestigeText);
                    continue;
                }
            }
            // Remove prefix if not holding a prestige katana
            updatePlayerNametag(player, "");
        }
    }

    private void updatePlayerNametag(Player player, String prefix) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "prestige_" + player.getName();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);
        
        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
        }
        
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        
        if (!team.getPrefix().equals(prefix)) {
            team.setPrefix(prefix);
        }
    }

    public void cleanUp() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String teamName = "prestige_" + player.getName();
            if (teamName.length() > 16) teamName = teamName.substring(0, 16);
            Team team = sb.getTeam(teamName);
            if (team != null) {
                team.unregister();
            }
        }
    }
}
