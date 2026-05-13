package pl.szympixel.katanamod;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {
    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration config;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "weapons_data.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getXP(UUID playerUUID, String katanaType) {
        return config.getInt("players." + playerUUID.toString() + "." + katanaType + ".xp", 0);
    }

    public void setXP(UUID playerUUID, String katanaType, int xp) {
        config.set("players." + playerUUID.toString() + "." + katanaType + ".xp", Math.max(0, xp));
        save();
    }

    public void addXP(UUID playerUUID, String katanaType, int amount) {
        int currentXP = getXP(playerUUID, katanaType);
        setXP(playerUUID, katanaType, currentXP + amount);
    }

    public boolean hasAllPrestige(UUID playerUUID) {
        String[] katanas = {"wakizashi", "tanto", "tachi", "odachi", "chisa"};
        for (String type : katanas) {
            if (getXP(playerUUID, type) < WeaponXPManager.Rank.PRESTIGE.getMinXP()) {
                return false;
            }
        }
        return true;
    }
}
