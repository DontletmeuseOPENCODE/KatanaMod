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
            meta.setLore(lore);

            meta.setCustomModelData(10002);

            NamespacedKey key = new NamespacedKey(plugin, KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "tanto");

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
    }
}
