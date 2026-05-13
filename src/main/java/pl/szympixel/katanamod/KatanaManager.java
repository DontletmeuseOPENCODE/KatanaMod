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

    public String getKatanaType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
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
            lore.add(ChatColor.GOLD + "[PPM] Dash do przodu (Cooldown: 5s)");
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
            lore.add(ChatColor.GOLD + "Cooldown: 20s");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10005);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "chisa");

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSmokeBomb() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Bomba Dymna");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Zasłona dymna ninja.");
            lore.add(ChatColor.WHITE + "Efekt: Tworzy kulę dymu na 10s");
            lore.add(ChatColor.GOLD + "Cooldown: 20s");
            lore.add(ChatColor.DARK_GRAY + "Do zdobycia na EVENCIE JAPONSKIM 2026");
            meta.setLore(lore);

            meta.setCustomModelData(10007);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "smokebomb");

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

        // Receptura Bomby Dymnej
        ItemStack smoke = createSmokeBomb();
        NamespacedKey smokeRecipeKey = new NamespacedKey(plugin, "smokebomb_recipe");
        ShapedRecipe smokeRecipe = new ShapedRecipe(smokeRecipeKey, smoke);
        smokeRecipe.shape(
                " G ",
                "GCG",
                " G "
        );
        smokeRecipe.setIngredient('G', Material.GUNPOWDER);
        smokeRecipe.setIngredient('C', Material.COAL);

        if (Bukkit.getRecipe(smokeRecipeKey) == null) {
            Bukkit.addRecipe(smokeRecipe);
        }

        // Receptura Ōdachi
        ItemStack odachi = createOdachi();
        NamespacedKey odachiRecipeKey = new NamespacedKey(plugin, "odachi_recipe");
        ShapedRecipe odachiRecipe = new ShapedRecipe(odachiRecipeKey, odachi);
        odachiRecipe.shape(
                "  D",
                " D ",
                "S  "
        );
        odachiRecipe.setIngredient('D', Material.DIAMOND);
        odachiRecipe.setIngredient('S', Material.STICK);

        if (Bukkit.getRecipe(odachiRecipeKey) == null) {
            Bukkit.addRecipe(odachiRecipe);
        }
    }
}
