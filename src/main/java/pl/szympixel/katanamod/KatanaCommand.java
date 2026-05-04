package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KatanaCommand implements CommandExecutor {
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

        if (args.length < 2) {
            if (args.length == 1 && args[0].equalsIgnoreCase("give")) {
                sender.sendMessage(ChatColor.RED + "Użycie: /katana give <gracz> <rodzaj (np. wakizashi)>");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Użycie: /katana give <gracz> <rodzaj>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("give")) {
             sender.sendMessage(ChatColor.RED + "Użycie: /katana give <gracz> <rodzaj>");
             return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Gracz nie jest online.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Podaj rodzaj katany (np. wakizashi).");
            return true;
        }

        String type = args[2].toLowerCase();

        ItemStack katanaItem = null;
        if (type.equals("wakizashi")) {
            katanaItem = katanaManager.createWakizashi();
        } else if (type.equals("tanto")) {
            katanaItem = katanaManager.createTanto();
        } else if (type.equals("tachi")) {
            katanaItem = katanaManager.createTachi();
        }

        if (katanaItem == null) {
            sender.sendMessage(ChatColor.RED + "Nieznany rodzaj katany. Dostępne: wakizashi, tanto, tachi");
            return true;
        }

        target.getInventory().addItem(katanaItem);
        sender.sendMessage(ChatColor.GREEN + "Przekazano katanę " + type + " graczowi " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "Otrzymałeś nową katanę: " + type + "!");

        return true;
    }
}
