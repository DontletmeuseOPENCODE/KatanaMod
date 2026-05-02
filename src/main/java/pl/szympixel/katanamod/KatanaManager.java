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
            lore.add(ChatColor.GREEN + "Efekt uderzenia: Trucizna (2 sek.)");
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
        if (Bukkit.getRecipe(recipeKey) == null) {
            Bukkit.addRecipe(recipe);
        }
    }
}
