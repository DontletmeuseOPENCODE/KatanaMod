package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KatanaModCommand implements CommandExecutor, TabCompleter {
    private final KatanaHitListener katanaHitListener;
    private final DataManager dataManager;
    private final KatanaManager katanaManager;
    private final BossBarManager bossBarManager;

    public KatanaModCommand(KatanaHitListener katanaHitListener, DataManager dataManager, KatanaManager katanaManager, BossBarManager bossBarManager) {
        this.katanaHitListener = katanaHitListener;
        this.dataManager = dataManager;
        this.katanaManager = katanaManager;
        this.bossBarManager = bossBarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) {
            sender.sendMessage(ChatColor.RED + "Nie masz uprawnień do użycia tej komendy.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            katanaHitListener.clearCooldowns();
            sender.sendMessage(ChatColor.GREEN + "[KatanaMod] Cooldowny zresetowane pomyślnie.");
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("set")) {
            String rankName = args[2].toUpperCase();
            Player target;
            
            if (args.length == 4) {
                target = Bukkit.getPlayer(args[3]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "Musisz podać gracza.");
                return true;
            }

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Nie znaleziono gracza.");
                return true;
            }

            WeaponXPManager.Rank rank;
            try {
                rank = WeaponXPManager.Rank.valueOf(rankName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Nieprawidłowa ranga. Dostępne: WOOD, BRONZE, SILVER, GOLD, PRESTIGE");
                return true;
            }

            ItemStack item = target.getInventory().getItemInMainHand();
            String katanaType = katanaManager.getKatanaType(item);
            if (katanaType == null) {
                sender.sendMessage(ChatColor.RED + "Gracz musi trzymać katanę w ręce!");
                return true;
            }

            dataManager.setXP(target.getUniqueId(), katanaType, rank.getMinXP());
            bossBarManager.updateBossBar(target, katanaType);
            sender.sendMessage(ChatColor.GREEN + "Ustawiono rangę " + rank.getName() + " dla " + target.getName() + " na katanie " + katanaType);
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Użycie:");
        sender.sendMessage(ChatColor.YELLOW + "/katanamod reload");
        sender.sendMessage(ChatColor.YELLOW + "/katanamod rank set <wood/bronze/silver/gold/prestige> [gracz]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) return Collections.emptyList();
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) completions.add("reload");
            if ("rank".startsWith(args[0].toLowerCase())) completions.add("rank");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("rank")) {
            if ("set".startsWith(args[1].toLowerCase())) return Arrays.asList("set");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("set")) {
            List<String> ranks = Arrays.asList("WOOD", "BRONZE", "SILVER", "GOLD", "PRESTIGE");
            List<String> completions = new ArrayList<>();
            for (String r : ranks) {
                if (r.toLowerCase().startsWith(args[2].toLowerCase())) completions.add(r.toLowerCase());
            }
            return completions;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("set")) {
            return null; // Return null for player completions
        }

        return Collections.emptyList();
    }
}
