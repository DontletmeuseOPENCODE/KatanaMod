package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class KatanaManager {
    private final JavaPlugin plugin;
    public static final String KATANA_TAG_KEY = "katana_type";

    public KatanaManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createWakizashi() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Wakizashi");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Krótki miecz samurajski.");
            lore.add(ChatColor.GREEN + "Efekt uderzenia: Trucizna (5 sek.)");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            // Ustawienie CustomModelData pod zasoby wizualne (Resource Pack)
            meta.setCustomModelData(10001);

            // Niewidoczny tag pozwalający zidentyfikować miecz jako konkretną katanę
            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "wakizashi");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTanto() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Tantō");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Krótkie ostrze o niesamowitej szybkości.");
            lore.add(ChatColor.AQUA + "Efekt uderzenia: Teleportacja za cel (Cooldown: 5s)");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10002);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "tanto");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createTachi() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Tachi");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Długi miecz konnicy. Daje przewagę w walce.");
            lore.add(ChatColor.GOLD + "[PPM] Dash do przodu (Cooldown: 20s)");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10003);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "tachi");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createOdachi() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Ōdachi");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Legendarny miecz pól bitewnych.");
            lore.add(ChatColor.LIGHT_PURPLE + "Tylko dla wybranych wojowników.");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            lore.add(ChatColor.AQUA + "[PPM] Pajęczyna pod wrogiem (Cooldown: 30s)");
            meta.setLore(lore);

            meta.setCustomModelData(10004);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "odachi");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createChisaKatana() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Chisa-katana");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Krótkie, zabójcze ostrze o krwistym kolorze.");
            lore.add(ChatColor.BLUE + "Efekt uderzenia: Zamrożenie (5s) + Ślepota (10s)");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10005);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "chisa");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createShuriken() {
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_BLUE + "Shuriken");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Szybka broń miotana ninja.");
            lore.add(ChatColor.GOLD + "Efekt: Podpalenie (1s)");
            lore.add(ChatColor.AQUA + "Powraca przy chybieniu!");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10006);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "shuriken");

            item.setItemMeta(meta);
        }
        return item;
    }

    public void registerRecipes() {
        ItemStack wakizashi = createWakizashi();
        NamespacedKey recipeKey = new NamespacedKey(plugin, "wakizashi_recipe");

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, wakizashi);
        recipe.shape(
                "  I",
                " I ",
                "S  "
        );
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);

        // Zarejestruj recepturę tylko, jeśli jeszcze jej nie ma
        // Receptura Wakizashi
        if (Bukkit.getRecipe(recipeKey) == null) {
            Bukkit.addRecipe(recipe);
        }

        // Receptura Tanto
        ItemStack tanto = createTanto();
        NamespacedKey tantoRecipeKey = new NamespacedKey(plugin, "tanto_recipe");
        ShapedRecipe tantoRecipe = new ShapedRecipe(tantoRecipeKey, tanto);
        tantoRecipe.shape(
                "  C",
                " C ",
                "S  "
        );
        tantoRecipe.setIngredient('C', Material.COPPER_INGOT);
        tantoRecipe.setIngredient('S', Material.STICK);

        if (Bukkit.getRecipe(tantoRecipeKey) == null) {
            Bukkit.addRecipe(tantoRecipe);
        }

        // Receptura Tachi
        ItemStack tachi = createTachi();
        NamespacedKey tachiRecipeKey = new NamespacedKey(plugin, "tachi_recipe");
        ShapedRecipe tachiRecipe = new ShapedRecipe(tachiRecipeKey, tachi);
        tachiRecipe.shape(
                "  G",
                " G ",
                "S  "
        );
        tachiRecipe.setIngredient('G', Material.GOLD_INGOT);
        tachiRecipe.setIngredient('S', Material.STICK);

        if (Bukkit.getRecipe(tachiRecipeKey) == null) {
            Bukkit.addRecipe(tachiRecipe);
        }

        // Receptura Chisa-katana
        ItemStack chisa = createChisaKatana();
        NamespacedKey chisaRecipeKey = new NamespacedKey(plugin, "chisa_recipe");
        ShapedRecipe chisaRecipe = new ShapedRecipe(chisaRecipeKey, chisa);
        chisaRecipe.shape(
                "  R",
                " R ",
                "S  "
        );
        chisaRecipe.setIngredient('R', Material.REDSTONE);
        chisaRecipe.setIngredient('S', Material.STICK);

        if (Bukkit.getRecipe(chisaRecipeKey) == null) {
            Bukkit.addRecipe(chisaRecipe);
        }

        // Receptura Shuriken
        ItemStack shuriken = createShuriken();
        NamespacedKey shurikenRecipeKey = new NamespacedKey(plugin, "shuriken_recipe");
        ShapedRecipe shurikenRecipe = new ShapedRecipe(shurikenRecipeKey, shuriken);
        shurikenRecipe.shape(
                " I ",
                "ISI",
                " I "
        );
        shurikenRecipe.setIngredient('I', Material.IRON_INGOT);
        shurikenRecipe.setIngredient('S', Material.IRON_NUGGET);

        if (Bukkit.getRecipe(shurikenRecipeKey) == null) {
            Bukkit.addRecipe(shurikenRecipe);
        }
    }
}
