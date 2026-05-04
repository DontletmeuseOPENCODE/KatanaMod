package pl.szympixel.katanamod;

import org.bukkit.plugin.java.JavaPlugin;

public class KatanaModPlugin extends JavaPlugin {
    private KatanaManager katanaManager;

    @Override
    public void onEnable() {
        getLogger().info("Inicjalizacja KatanaMod...");

        this.katanaManager = new KatanaManager(this);
        this.katanaManager.registerRecipes();

        KatanaHitListener hitListener = new KatanaHitListener(this);
        getServer().getPluginManager().registerEvents(hitListener, this);

        KatanaCommand katanaCmd = new KatanaCommand(katanaManager);
        if (getCommand("katana") != null) {
            getCommand("katana").setExecutor(katanaCmd);
            getCommand("katana").setTabCompleter(katanaCmd);
        }

        KatanaModCommand katanaModCmd = new KatanaModCommand(hitListener);
        if (getCommand("katanamod") != null) {
            getCommand("katanamod").setExecutor(katanaModCmd);
            getCommand("katanamod").setTabCompleter(katanaModCmd);
        }

        getLogger().info("KatanaMod 1.3 uruchomiony pomyślnie!");
    }

    @Override
    public void onDisable() {
        getLogger().info("KatanaMod został wyłączony.");
    }
}
