package pl.szympixel.katanamod;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KatanaModCommand implements CommandExecutor, TabCompleter {
    private final KatanaHitListener katanaHitListener;

    public KatanaModCommand(KatanaHitListener katanaHitListener) {
        this.katanaHitListener = katanaHitListener;
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

        sender.sendMessage(ChatColor.YELLOW + "Użycie: /katanamod reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) return Collections.emptyList();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) return Arrays.asList("reload");
        }
        return Collections.emptyList();
    }
}
