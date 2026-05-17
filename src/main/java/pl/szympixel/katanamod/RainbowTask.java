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
        ChatColor rainbowColor = colors[colorIndex];
        colorIndex = (colorIndex + 1) % colors.length;

        for (Player player : Bukkit.getOnlinePlayers()) {
            int totalPrestige = getTotalPrestiges(player.getUniqueId());
            
            ItemStack item = player.getInventory().getItemInMainHand();
            String katanaType = katanaManager.getKatanaType(item);
            
            if (katanaType != null) {
                // Gracz trzyma katanę
                int xp = dataManager.getXP(player.getUniqueId(), katanaType);
                WeaponXPManager.Rank rank = WeaponXPManager.getRank(xp);
                
                if (rank == WeaponXPManager.Rank.PRESTIGE) {
                    int prestige = WeaponXPManager.getPrestigeLevel(xp);
                    ChatColor pColor = getPrestigeColor(prestige);
                    String prestigeText = ChatColor.GRAY + "[" + pColor + "Prestiż " + prestige + ChatColor.GRAY + "] ";
                    updatePlayerNametag(player, prestigeText, totalPrestige);
                } else {
                    String rankText = ChatColor.GRAY + "[" + rank.getColor() + rank.getName() + ChatColor.GRAY + "] ";
                    updatePlayerNametag(player, rankText, totalPrestige);
                }
            } else {
                // Gracz nie trzyma katany - pokazujemy sumę prestiży na tęczowo
                if (totalPrestige > 0) {
                    String totalText = ChatColor.GRAY + "[" + rainbowColor + totalPrestige + " Prestiży" + ChatColor.GRAY + "] ";
                    updatePlayerNametag(player, totalText, totalPrestige);
                } else {
                    updatePlayerNametag(player, "", totalPrestige);
                }
            }
        }
    }

    private int getTotalPrestiges(UUID uuid) {
        int total = 0;
        String[] katanas = {"wakizashi", "tanto", "tachi", "odachi", "chisa", "smokebomb"};
        for (String k : katanas) {
            int xp = dataManager.getXP(uuid, k);
            if (WeaponXPManager.getRank(xp) == WeaponXPManager.Rank.PRESTIGE) {
                total += 1; // Każda broń z prestiżem daje 1 punkt do ogólnego licznika
            }
        }
        return total;
    }

    private ChatColor getPrestigeColor(int level) {
        if (level == 1) return ChatColor.LIGHT_PURPLE;
        if (level == 2) return ChatColor.RED;
        return ChatColor.YELLOW; // 3 i więcej
    }

    private void updatePlayerNametag(Player player, String prefix, int totalPrestige) {
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

        // Sufiks - czyścimy (bez znaczników zdrowia)
        if (!team.getSuffix().isEmpty()) {
            team.setSuffix("");
        }

        // Cel "BELOW_NAME" - pokazywanie ilości prestiży pod nickiem (tylko jeśli > 0)
        org.bukkit.scoreboard.Objective obj = sb.getObjective("prestiges");
        if (totalPrestige > 0) {
            if (obj == null) {
                obj = sb.registerNewObjective("prestiges", "dummy", ChatColor.LIGHT_PURPLE + "Prestiży");
                obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.BELOW_NAME);
            }
            obj.getScore(player.getName()).setScore(totalPrestige);
        } else {
            // Gracz ma 0 prestiży - resetuj jego wynik
            if (obj != null) {
                sb.resetScores(player.getName());
            }
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
        org.bukkit.scoreboard.Objective obj = sb.getObjective("prestiges");
        if (obj != null) {
            obj.unregister();
        }
    }
}
