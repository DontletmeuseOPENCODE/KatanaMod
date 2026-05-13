package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BossBarManager {
    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final HashMap<UUID, BossBar> activeBars = new HashMap<>();

    public BossBarManager(JavaPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void updateBossBar(Player player, String katanaType) {
        if (katanaType == null) {
            removeBossBar(player);
            return;
        }

        int xp = dataManager.getXP(player.getUniqueId(), katanaType);
        WeaponXPManager.Rank rank = WeaponXPManager.getRank(xp);
        int nextXP = WeaponXPManager.getNextRankXP(xp);
        int prevXP = rank.getMinXP();

        double progress = 0;
        if (rank == WeaponXPManager.Rank.PRESTIGE) {
            int level = WeaponXPManager.getPrestigeLevel(xp);
            int xpInLevel = xp - prevXP;
            int nextLevelXP = nextXP - prevXP;
            progress = (double) xpInLevel / nextLevelXP;
        } else {
            progress = (double) (xp - prevXP) / (nextXP - prevXP);
        }
        progress = Math.min(1.0, Math.max(0.0, progress));

        String title = rank.getColor() + rank.getName() + " " + ChatColor.GRAY + "| XP: " + 
                       rank.getColor() + xp + ChatColor.DARK_GRAY + " / " + nextXP;
        
        if (rank == WeaponXPManager.Rank.PRESTIGE) {
            title = rank.getColor() + "PRESTIŻ " + WeaponXPManager.getPrestigeLevel(xp) + 
                    ChatColor.GRAY + " | XP: " + rank.getColor() + xp;
        }

        BossBar bar = activeBars.get(player.getUniqueId());
        if (bar == null) {
            bar = Bukkit.createBossBar(title, getBarColor(rank), BarStyle.SOLID);
            bar.addPlayer(player);
            activeBars.put(player.getUniqueId(), bar);
        } else {
            bar.setTitle(title);
            bar.setColor(getBarColor(rank));
            bar.setProgress(progress);
        }
        bar.setVisible(true);
    }

    public void removeBossBar(Player player) {
        BossBar bar = activeBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private BarColor getBarColor(WeaponXPManager.Rank rank) {
        switch (rank) {
            case WOOD: return BarColor.WHITE;
            case BRONZE: return BarColor.YELLOW;
            case SILVER: return BarColor.WHITE;
            case GOLD: return BarColor.YELLOW;
            case PRESTIGE: return BarColor.PURPLE;
            default: return BarColor.WHITE;
        }
    }
}
