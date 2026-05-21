package pl.szympixel.katanamod;

import org.bukkit.plugin.java.JavaPlugin;

public class KatanaModPlugin extends JavaPlugin {
    private KatanaManager katanaManager;
    private DataManager dataManager;
    private BossBarManager bossBarManager;
    private ManaManager manaManager;
    private KatanaEnchantmentManager enchantmentManager;
    private RainbowTask rainbowTask;

    @Override
    public void onEnable() {
        getLogger().info("Inicjalizacja KatanaMod 1.5...");

        // Managers
        this.katanaManager = new KatanaManager(this);
        this.dataManager = new DataManager(this);
        this.bossBarManager = new BossBarManager(this, dataManager);
        this.manaManager = new ManaManager(this, katanaManager);
        this.enchantmentManager = new KatanaEnchantmentManager(this, katanaManager);
        
        // Recipes
        this.katanaManager.registerRecipes();

        // Listeners
        KatanaHitListener hitListener = new KatanaHitListener(this, manaManager, enchantmentManager);
        getServer().getPluginManager().registerEvents(hitListener, this);
        
        PrestigeListener prestigeListener = new PrestigeListener(this, dataManager, bossBarManager, katanaManager);
        getServer().getPluginManager().registerEvents(prestigeListener, this);

        KatanaMergerListener mergerListener = new KatanaMergerListener(this, katanaManager);
        getServer().getPluginManager().registerEvents(mergerListener, this);

        // Commands
        KatanaCommand katanaCmd = new KatanaCommand(katanaManager, this);
        if (getCommand("katana") != null) {
            getCommand("katana").setExecutor(katanaCmd);
            getCommand("katana").setTabCompleter(katanaCmd);
        }

        KatanaModCommand katanaModCmd = new KatanaModCommand(hitListener, dataManager, katanaManager, bossBarManager);
        if (getCommand("katanamod") != null) {
            getCommand("katanamod").setExecutor(katanaModCmd);
            getCommand("katanamod").setTabCompleter(katanaModCmd);
        }

        // Tasks
        this.rainbowTask = new RainbowTask(this, dataManager, katanaManager);
        this.rainbowTask.runTaskTimer(this, 0L, 5L);

        getLogger().info("KatanaMod 1.5 uruchomiony pomyślnie!");
    }

    @Override
    public void onDisable() {
        if (this.rainbowTask != null) {
            this.rainbowTask.cleanUp();
        }
        if (this.dataManager != null) {
            this.dataManager.save();
        }
        getLogger().info("KatanaMod został wyłączony.");
    }

    public KatanaManager getKatanaManager() {
        return katanaManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public ManaManager getManaManager() {
        return manaManager;
    }

    public KatanaEnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }
}
