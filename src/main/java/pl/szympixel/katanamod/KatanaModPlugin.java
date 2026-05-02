package pl.szympixel.katanamod;

import org.bukkit.plugin.java.JavaPlugin;

public class KatanaModPlugin extends JavaPlugin {
    private KatanaManager katanaManager;

    @Override
    public void onEnable() {
        getLogger().info("Inicjalizacja KatanaMod...");

        this.katanaManager = new KatanaManager(this);
        // Rejestracja craftingu
        this.katanaManager.registerRecipes();

        // Rejestracja listenera do ataków
        getServer().getPluginManager().registerEvents(new KatanaHitListener(this), this);
        
        // Rejestracja komendy
        if (getCommand("katana") != null) {
            getCommand("katana").setExecutor(new KatanaCommand(katanaManager));
        }

        getLogger().info("KatanaMod został pomyślnie uruchomiony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("KatanaMod został wyłączony.");
    }
}
