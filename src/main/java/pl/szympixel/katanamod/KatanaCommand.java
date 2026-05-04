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
import java.util.stream.Collectors;

public class KatanaCommand implements CommandExecutor, TabCompleter {
    private static final List<String> KATANA_TYPES = Arrays.asList("wakizashi", "tanto", "tachi", "odachi");
    private final KatanaManager katanaManager;

    public KatanaCommand(KatanaManager katanaManager) {
        this.katanaManager = katanaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) {
            sender.sendMessage(ChatColor.RED + "Nie masz uprawnień do użycia tej komendy.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(ChatColor.YELLOW + "--- KatanaMod ---");
            sender.sendMessage(ChatColor.GRAY + "/katana give <gracz> <" + String.join("|", KATANA_TYPES) + ">");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Użycie: /katana give <gracz> <" + String.join("|", KATANA_TYPES) + ">");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Gracz '" + args[1] + "' nie jest online.");
            return true;
        }

        String type = args[2].toLowerCase();
        ItemStack katanaItem = null;
        switch (type) {
            case "wakizashi": katanaItem = katanaManager.createWakizashi(); break;
            case "tanto":     katanaItem = katanaManager.createTanto();     break;
            case "tachi":     katanaItem = katanaManager.createTachi();     break;
            case "odachi":    katanaItem = katanaManager.createOdachi();    break;
        }

        if (katanaItem == null) {
            sender.sendMessage(ChatColor.RED + "Nieznana katana. Dostępne: " + String.join(", ", KATANA_TYPES));
            return true;
        }

        target.getInventory().addItem(katanaItem);
        sender.sendMessage(ChatColor.GREEN + "Przekazano " + type + " graczowi " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "Otrzymałeś katanę: " + ChatColor.YELLOW + type + ChatColor.GREEN + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return filterStarting(Collections.singletonList("give"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Zwraca listę graczy online
            return null;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filterStarting(KATANA_TYPES, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filterStarting(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
