package pl.szympixel.katanamod;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class KatanaEnchantmentManager {
    private final JavaPlugin plugin;
    private final KatanaManager katanaManager;

    // Keys
    public final NamespacedKey kensaiKey;
    public final NamespacedKey soulStealerKey;
    public final NamespacedKey windBladeKey;
    public final NamespacedKey meditationKey;
    public final NamespacedKey staticShockKey;

    private final Map<String, NamespacedKey> enchantKeys = new HashMap<>();
    private final Map<String, String> enchantNames = new HashMap<>();
    private final Map<String, Integer> enchantMaxLevels = new HashMap<>();

    public KatanaEnchantmentManager(JavaPlugin plugin, KatanaManager katanaManager) {
        this.plugin = plugin;
        this.katanaManager = katanaManager;

        this.kensaiKey = new NamespacedKey(plugin, "enchant_kensai");
        this.soulStealerKey = new NamespacedKey(plugin, "enchant_soulstealer");
        this.windBladeKey = new NamespacedKey(plugin, "enchant_windblade");
        this.meditationKey = new NamespacedKey(plugin, "enchant_meditation");
        this.staticShockKey = new NamespacedKey(plugin, "enchant_staticshock");

        enchantKeys.put("kensai", kensaiKey);
        enchantKeys.put("soulstealer", soulStealerKey);
        enchantKeys.put("windblade", windBladeKey);
        enchantKeys.put("meditation", meditationKey);
        enchantKeys.put("staticshock", staticShockKey);

        enchantNames.put("kensai", "Kensai");
        enchantNames.put("soulstealer", "Pożeracz Dusz");
        enchantNames.put("windblade", "Ostrze Wiatru");
        enchantNames.put("meditation", "Medytacja");
        enchantNames.put("staticshock", "Wyładowanie");

        enchantMaxLevels.put("kensai", 3);
        enchantMaxLevels.put("soulstealer", 2);
        enchantMaxLevels.put("windblade", 2);
        enchantMaxLevels.put("meditation", 3);
        enchantMaxLevels.put("staticshock", 2);
    }

    public boolean isKatana(ItemStack item) {
        return katanaManager.getKatanaType(item) != null;
    }

    public NamespacedKey getKey(String name) {
        return enchantKeys.get(name.toLowerCase());
    }

    public String getDisplayName(String name) {
        return enchantNames.get(name.toLowerCase());
    }

    public int getMaxLevel(String name) {
        return enchantMaxLevels.getOrDefault(name.toLowerCase(), 1);
    }

    public Collection<String> getAvailableEnchants() {
        return enchantKeys.keySet();
    }

    public int getLevel(ItemStack item, String enchantName) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        NamespacedKey key = getKey(enchantName);
        if (key == null) return 0;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.INTEGER)) {
            return container.get(key, PersistentDataType.INTEGER);
        }
        return 0;
    }

    public void applyEnchantment(ItemStack item, String enchantName, int level) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = getKey(enchantName);
        if (key == null) return;

        int maxLvl = getMaxLevel(enchantName);
        int finalLvl = Math.max(1, Math.min(maxLvl, level));

        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, finalLvl);
        item.setItemMeta(meta);

        // Update attributes if Kensai
        if ("kensai".equalsIgnoreCase(enchantName)) {
            updateKensaiAttributes(item, finalLvl);
        } else {
            // Re-apply existing kensai if attributes were reset or need update
            int kensaiLvl = getLevel(item, "kensai");
            updateKensaiAttributes(item, kensaiLvl);
        }

        updateLore(item);
    }

    public void removeEnchantment(ItemStack item, String enchantName) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = getKey(enchantName);
        if (key == null) return;

        meta.getPersistentDataContainer().remove(key);
        item.setItemMeta(meta);

        if ("kensai".equalsIgnoreCase(enchantName)) {
            updateKensaiAttributes(item, 0);
        }

        updateLore(item);
    }

    private void updateKensaiAttributes(ItemStack item, int kensaiLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        // Default attributes
        double baseDamageModifier = 5.0; // Default modifier for iron sword (+6 damage total)
        if (item.getType() == Material.STONE_SWORD) {
            baseDamageModifier = 4.0; // Stone sword (+5 damage total)
        }

        // Base attack speed for any sword is 1.6 (meaning modifier is -2.4 compared to base 4.0)
        // Kensai increases attack speed by +15% per level.
        // 15% of 1.6 is 0.24. So level 1 is +0.24, level 2 is +0.48, level 3 is +0.72.
        double baseSpeedModifier = -2.4;
        double kensaiBonus = 0.24 * kensaiLevel;
        double finalSpeedModifier = baseSpeedModifier + kensaiBonus;

        UUID damageUuid = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
        UUID speedUuid = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(
                damageUuid, "Tool Damage", baseDamageModifier, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(
                speedUuid, "Tool Speed", finalSpeedModifier, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        item.setItemMeta(meta);
    }

    public void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Remove existing enchantment lore lines
        lore.removeIf(line -> line.contains("🔮"));

        // Gather active enchantments
        List<String> enchantLoreLines = new ArrayList<>();
        for (String enchant : enchantKeys.keySet()) {
            int lvl = getLevel(item, enchant);
            if (lvl > 0) {
                String roman = toRoman(lvl);
                enchantLoreLines.add(ChatColor.LIGHT_PURPLE + "🔮 " + getDisplayName(enchant) + " " + roman);
            }
        }

        if (!enchantLoreLines.isEmpty()) {
            // Find insertion point - before the footer or separator
            int insertIndex = lore.size();
            
            // Check if there is a footer line like "Squash & Merge™" or event tag
            for (int i = lore.size() - 1; i >= 0; i--) {
                String line = ChatColor.stripColor(lore.get(i));
                if (line.contains("Squash & Merge") || line.contains("EVENCIE JAPONSKIM") || line.contains("━━━━━━━━━━━━━━━━━━━")) {
                    insertIndex = i;
                }
            }

            // Insert separator and enchantments
            lore.add(insertIndex, ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.addAll(insertIndex + 1, enchantLoreLines);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public ItemStack createEnchantmentBook(String enchantName, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = getDisplayName(enchantName);
            String roman = toRoman(level);
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Księga Zaklęcia: " + name + " " + roman);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Połącz tę księgę z kataną w kowadle");
            lore.add(ChatColor.GRAY + "Squash & Merge, aby nałożyć ulepszenie.");
            lore.add(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.LIGHT_PURPLE + "🔮 " + name + " " + roman);
            lore.add(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.DARK_GRAY + "Squash & Merge™");
            meta.setLore(lore);
            
            int cmd = 10020;
            switch (enchantName.toLowerCase()) {
                case "kensai": cmd = 10021; break;
                case "soulstealer": cmd = 10022; break;
                case "windblade": cmd = 10023; break;
                case "meditation": cmd = 10024; break;
                case "staticshock": cmd = 10025; break;
            }
            meta.setCustomModelData(cmd);
            
            NamespacedKey typeKey = new NamespacedKey(plugin, "enchant_type");
            NamespacedKey lvlKey = new NamespacedKey(plugin, "enchant_level");
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, enchantName.toLowerCase());
            meta.getPersistentDataContainer().set(lvlKey, PersistentDataType.INTEGER, level);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    private String toRoman(int num) {
        switch (num) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(num);
        }
    }
}
