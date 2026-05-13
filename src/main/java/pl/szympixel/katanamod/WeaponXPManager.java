package pl.szympixel.katanamod;

import org.bukkit.ChatColor;

public class WeaponXPManager {

    public enum Rank {
        WOOD("Drewno", ChatColor.DARK_GRAY, 0, 0.05),
        BRONZE("Brąz", ChatColor.GOLD, 100, 0.10),
        SILVER("Srebro", ChatColor.GRAY, 300, 0.15),
        GOLD("Złoto", ChatColor.YELLOW, 600, 0.20),
        PRESTIGE("Prestiż", ChatColor.LIGHT_PURPLE, 1000, 0.0);

        private final String name;
        private final ChatColor color;
        private final int minXP;
        private final double deathLoss;

        Rank(String name, ChatColor color, int minXP, double deathLoss) {
            this.name = name;
            this.color = color;
            this.minXP = minXP;
            this.deathLoss = deathLoss;
        }

        public String getName() {
            return name;
        }

        public ChatColor getColor() {
            return color;
        }

        public int getMinXP() {
            return minXP;
        }

        public double getDeathLoss() {
            return deathLoss;
        }
    }

    public static Rank getRank(int xp) {
        if (xp >= Rank.PRESTIGE.minXP) return Rank.PRESTIGE;
        if (xp >= Rank.GOLD.minXP) return Rank.GOLD;
        if (xp >= Rank.SILVER.minXP) return Rank.SILVER;
        if (xp >= Rank.BRONZE.minXP) return Rank.BRONZE;
        return Rank.WOOD;
    }

    public static int getNextRankXP(int xp) {
        Rank current = getRank(xp);
        switch (current) {
            case WOOD: return Rank.BRONZE.minXP;
            case BRONZE: return Rank.SILVER.minXP;
            case SILVER: return Rank.GOLD.minXP;
            case GOLD: return Rank.PRESTIGE.minXP;
            case PRESTIGE: 
                int prestigeLevel = getPrestigeLevel(xp);
                return Rank.PRESTIGE.minXP + (prestigeLevel * 1000);
            default: return Rank.PRESTIGE.minXP;
        }
    }

    public static int getPrestigeLevel(int xp) {
        if (xp < Rank.PRESTIGE.minXP) return 0;
        return ((xp - Rank.PRESTIGE.minXP) / 1000) + 1;
    }
}
