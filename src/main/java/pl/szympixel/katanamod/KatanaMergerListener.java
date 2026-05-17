package pl.szympixel.katanamod;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class KatanaMergerListener implements Listener {
    private final KatanaModPlugin plugin;
    private final KatanaManager katanaManager;
    private final NamespacedKey katanaKey;
    private final NamespacedKey mergedEffectsKey;
    private final Set<UUID> openMergers = new HashSet<>();

    public static final String MERGER_TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Squash & Merge";

    private static final String[] ALL_EFFECTS = {
        "poison", "teleport", "dash", "cobweb", "freeze", "invisible"
    };

    private static final Map<String, String> EFFECT_NAMES = new LinkedHashMap<>();
    static {
        EFFECT_NAMES.put("poison", ChatColor.DARK_GREEN + "☠ Trucizna (5s) przy uderzeniu");
        EFFECT_NAMES.put("teleport", ChatColor.AQUA + "⚡ Teleportacja za cel (CD: 5s)");
        EFFECT_NAMES.put("dash", ChatColor.YELLOW + "💨 Dash do przodu [PPM] (CD: 5s)");
        EFFECT_NAMES.put("cobweb", ChatColor.DARK_PURPLE + "🕸 Pajęczyna pod wrogiem [PPM] (CD: 30s)");
        EFFECT_NAMES.put("freeze", ChatColor.BLUE + "❄ Zamrożenie (5s) + Ślepota (10s) (CD: 20s)");
        EFFECT_NAMES.put("invisible", ChatColor.GRAY + "👻 Niewidzialność (5s) przy uderzeniu");
    }

    public KatanaMergerListener(KatanaModPlugin plugin, KatanaManager katanaManager) {
        this.plugin = plugin;
        this.katanaManager = katanaManager;
        this.katanaKey = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
        this.mergedEffectsKey = new NamespacedKey(plugin, "merged_effects");
    }

    // Shift + PPM na kowadle = otwiera Katana Merger
    @EventHandler
    public void onAnvilClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();
        if (blockType != Material.ANVIL && blockType != Material.CHIPPED_ANVIL && blockType != Material.DAMAGED_ANVIL) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        event.setCancelled(true);
        @SuppressWarnings("deprecation")
        Inventory inv = Bukkit.createInventory(null, InventoryType.ANVIL, MERGER_TITLE);
        player.openInventory(inv);
        openMergers.add(player.getUniqueId());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Katana Merger otwarty! Połóż dwie katany, aby je połączyć.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!openMergers.contains(player.getUniqueId())) return;
        if (!event.getView().getTitle().equals(MERGER_TITLE)) return;

        Inventory inv = event.getView().getTopInventory();

        // Sloty 0 i 1 - pozwól wkładać/wyjmować
        if (event.getRawSlot() == 0 || event.getRawSlot() == 1) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateResult(inv), 1L);
            return;
        }

        // Slot 2 - wynik
        if (event.getRawSlot() == 2) {
            ItemStack result = inv.getItem(2);
            if (result == null || result.getType() == Material.AIR) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            player.getInventory().addItem(result.clone());
            inv.setItem(0, null);
            inv.setItem(1, null);
            inv.setItem(2, null);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Katany zostały połączone! Powstała nowa, hybrydowa broń!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
            return;
        }

        // Kliknięcia w inventorz gracza - pozwalamy i odświeżamy
        if (event.getRawSlot() >= inv.getSize()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateResult(inv), 1L);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (!openMergers.remove(player.getUniqueId())) return;

        Inventory inv = event.getInventory();
        // Zwróć inputy graczowi
        for (int i = 0; i < 2; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }
    }

    private void updateResult(Inventory inv) {
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (!isKatana(first) || !isKatana(second)) {
            inv.setItem(2, null);
            return;
        }

        // Obie są katanami - stwórz wynik
        inv.setItem(2, createMergedKatana());
    }

    private boolean isKatana(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(katanaKey, PersistentDataType.STRING);
    }

    public ItemStack createMergedKatana() {
        ItemStack item = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "混合カタナ" +
                    ChatColor.RESET + ChatColor.GRAY + " (Merged Katana)");

            // Losuj 2 różne efekty
            List<String> effectPool = new ArrayList<>(Arrays.asList(ALL_EFFECTS));
            Collections.shuffle(effectPool);
            String effect1 = effectPool.get(0);
            String effect2 = effectPool.get(1);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.LIGHT_PURPLE + "Hybrydowa katana stworzona");
            lore.add(ChatColor.LIGHT_PURPLE + "przez połączenie dwóch ostrzy.");
            lore.add(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.GOLD + "Efekty:");
            lore.add(EFFECT_NAMES.get(effect1));
            lore.add(EFFECT_NAMES.get(effect2));
            lore.add(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.DARK_GRAY + "Squash & Merge™");
            meta.setLore(lore);

            // Enchantment glint
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            meta.setCustomModelData(10010);

            // Oznaczenie jako katana typu "merged" + zapis efektów
            NamespacedKey key = new NamespacedKey(plugin, KatanaManager.KATANA_TAG_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "merged");
            meta.getPersistentDataContainer().set(mergedEffectsKey, PersistentDataType.STRING, effect1 + "," + effect2);

            item.setItemMeta(meta);
        }
        return item;
    }
}
