package pl.szympixel.katanamod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KatanaCommand implements CommandExecutor, TabCompleter {
    private static final List<String> KATANA_TYPES = Arrays.asList("wakizashi", "tanto", "tachi", "odachi", "chisa", "smokebomb", "merged");
    private final KatanaManager katanaManager;
    private final JavaPlugin plugin;
    private String language = "PL";
    private boolean adminEnabled = true;

    public KatanaCommand(KatanaManager katanaManager, JavaPlugin plugin) {
        this.katanaManager = katanaManager;
        this.plugin = plugin;
    }

    private String getMsg(String key) {
        if (language.equals("EN")) {
            switch (key) {
                case "no_perm": return ChatColor.RED + "You don't have permission to use this command.";
                case "usage": return ChatColor.YELLOW + "--- KatanaMod ---" + "\n" + ChatColor.GRAY + "/katana give <player> <type>\n/katana enchant <enchant> [level]\n/katana version\n/katana language <PL|EN>\n/katana on/off\n/katana github\n/katana website\n/katana rank set <player> <type> <rank>\n/katana prestige set <player> <type> <number>";
                case "player_offline": return ChatColor.RED + "Player '%s' is not online.";
                case "given": return ChatColor.GREEN + "Gave %s to player %s.";
                case "received": return ChatColor.GREEN + "You received a katana: " + ChatColor.YELLOW + "%s" + ChatColor.GREEN + "!";
                case "unknown": return ChatColor.RED + "Unknown katana. Available: " + String.join(", ", KATANA_TYPES);
                case "lang_changed": return ChatColor.GREEN + "Language changed to English.";
                case "version": return ChatColor.AQUA + "KatanaMod Version: " + ChatColor.YELLOW + plugin.getDescription().getVersion() + 
                                       "\n" + ChatColor.GOLD + "Credits: FriskieBOI & szympixel.pl team";
                case "admin_on": return ChatColor.GREEN + "Admin commands enabled.";
                case "admin_off": return ChatColor.RED + "Admin commands disabled.";
                case "admin_disabled": return ChatColor.RED + "Admin commands are currently disabled by the server.";
                case "click_link": return ChatColor.AQUA + "Click here to open: ";
                default: return "";
            }
        } else {
            switch (key) {
                case "no_perm": return ChatColor.RED + "Nie masz uprawnień do użycia tej komendy.";
                case "usage": return ChatColor.YELLOW + "--- KatanaMod ---" + "\n" + ChatColor.GRAY + "/katana give <gracz> <typ>\n/katana enchant <zaklęcie> [poziom]\n/katana version\n/katana language <PL|EN>\n/katana on/off\n/katana github\n/katana website\n/katana rank set <gracz> <typ> <ranga>\n/katana prestige set <gracz> <typ> <numer>";
                case "player_offline": return ChatColor.RED + "Gracz '%s' nie jest online.";
                case "given": return ChatColor.GREEN + "Przekazano %s graczowi %s.";
                case "received": return ChatColor.GREEN + "Otrzymałeś katanę: " + ChatColor.YELLOW + "%s" + ChatColor.GREEN + "!";
                case "unknown": return ChatColor.RED + "Nieznana katana. Dostępne: " + String.join(", ", KATANA_TYPES);
                case "lang_changed": return ChatColor.GREEN + "Język został zmieniony na Polski.";
                case "version": return ChatColor.AQUA + "Wersja KatanaMod: " + ChatColor.YELLOW + plugin.getDescription().getVersion() + 
                                       "\n" + ChatColor.GOLD + "Autorzy: FriskieBOI & szympixel.pl team";
                case "admin_on": return ChatColor.GREEN + "Komendy admina zostały włączone.";
                case "admin_off": return ChatColor.RED + "Komendy admina zostały wyłączone.";
                case "admin_disabled": return ChatColor.RED + "Komendy admina są obecnie wyłączone przez serwer.";
                case "click_link": return ChatColor.AQUA + "Kliknij tutaj, aby otworzyć: ";
                default: return "";
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Linki działają zawsze dla każdego
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("github")) {
                sendClickableLink(sender, "https://github.com/DontletmeuseOPENCODE/KatanaMod", "GitHub");
                return true;
            }
            if (args[0].equalsIgnoreCase("website")) {
                sendClickableLink(sender, "https://szympixel.pl", "SzymPixel.pl");
                return true;
            }
        }

        if (!sender.hasPermission("katanamod.admin")) {
            sender.sendMessage(getMsg("no_perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(getMsg("usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            adminEnabled = true;
            sender.sendMessage(getMsg("admin_on"));
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            adminEnabled = false;
            sender.sendMessage(getMsg("admin_off"));
            return true;
        }

        if (!adminEnabled && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("version") && !args[0].equalsIgnoreCase("language")) {
            sender.sendMessage(getMsg("admin_disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(getMsg("version"));
            return true;
        }

        if (args[0].equalsIgnoreCase("language") && args.length >= 2) {
            String newLang = args[1].toUpperCase();
            if (newLang.equals("PL") || newLang.equals("EN")) {
                this.language = newLang;
                sender.sendMessage(getMsg("lang_changed"));
            } else {
                sender.sendMessage(ChatColor.RED + "Użycie/Usage: /katana language <PL|EN>");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage(getMsg("usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(String.format(getMsg("player_offline"), args[1]));
                return true;
            }

            String type = args[2].toLowerCase();
            ItemStack katanaItem = null;
            switch (type) {
                case "wakizashi": katanaItem = katanaManager.createWakizashi(); break;
                case "tanto":     katanaItem = katanaManager.createTanto();     break;
                case "tachi":     katanaItem = katanaManager.createTachi();     break;
                case "odachi":    katanaItem = katanaManager.createOdachi();    break;
                case "chisa":     katanaItem = katanaManager.createChisaKatana(); break;
                case "smokebomb": katanaItem = katanaManager.createSmokeBomb(); break;
                case "merged":    katanaItem = new KatanaMergerListener((KatanaModPlugin) plugin, katanaManager).createMergedKatana(); break;
            }

            if (katanaItem == null && type.startsWith("book:")) {
                String[] parts = type.split(":");
                if (parts.length >= 2) {
                    String enchantName = parts[1];
                    int level = 1;
                    if (parts.length >= 3) {
                        try {
                            level = Integer.parseInt(parts[2]);
                        } catch (NumberFormatException ignored) {}
                    }
                    KatanaEnchantmentManager em = ((KatanaModPlugin) plugin).getEnchantmentManager();
                    if (em != null && em.getKey(enchantName) != null) {
                        katanaItem = em.createEnchantmentBook(enchantName, level);
                    }
                }
            }

            if (katanaItem == null) {
                sender.sendMessage(getMsg("unknown"));
                return true;
            }

            target.getInventory().addItem(katanaItem);
            sender.sendMessage(String.format(getMsg("given"), type, target.getName()));
            target.sendMessage(String.format(getMsg("received"), type));
            return true;
        }

        if (args[0].equalsIgnoreCase("rank")) {
            if (args.length >= 5 && args[1].equalsIgnoreCase("set")) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(String.format(getMsg("player_offline"), args[2]));
                    return true;
                }

                String type = args[3].toLowerCase();
                if (!KATANA_TYPES.contains(type)) {
                    sender.sendMessage(getMsg("unknown"));
                    return true;
                }

                String rankName = args[4].toUpperCase();
                try {
                    WeaponXPManager.Rank rank = WeaponXPManager.Rank.valueOf(rankName);
                    DataManager dataManager = ((KatanaModPlugin) plugin).getDataManager();
                    dataManager.setXP(target.getUniqueId(), type, rank.getMinXP());
                    
                    String msg = language.equals("EN") ? 
                        ChatColor.GREEN + "Rank for " + target.getName() + " on " + type + " set to " + rank.getName() :
                        ChatColor.GREEN + "Ranga gracza " + target.getName() + " dla " + type + " ustawiona na " + rank.getName();
                    sender.sendMessage(msg);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Nieznana ranga / Unknown rank! Dostępne/Available: WOOD, BRONZE, SILVER, GOLD, PRESTIGE");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Użycie/Usage: /katana rank set <player> <type> <rank>");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("prestige")) {
            if (args.length >= 5 && args[1].equalsIgnoreCase("set")) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(String.format(getMsg("player_offline"), args[2]));
                    return true;
                }

                String type = args[3].toLowerCase();
                if (!KATANA_TYPES.contains(type)) {
                    sender.sendMessage(getMsg("unknown"));
                    return true;
                }

                try {
                    int prestigeLevel = Integer.parseInt(args[4]);
                    if (prestigeLevel < 1 || prestigeLevel > 100) {
                        sender.sendMessage(ChatColor.RED + "Poziom prestiżu musi być w przedziale od 1 do 100.");
                        return true;
                    }
                    
                    DataManager dataManager = ((KatanaModPlugin) plugin).getDataManager();
                    // Prestiż level X to minXP + (X-1)*1000, więc:
                    int requiredXP = WeaponXPManager.Rank.PRESTIGE.getMinXP() + ((prestigeLevel - 1) * 1000);
                    dataManager.setXP(target.getUniqueId(), type, requiredXP);
                    
                    String msg = language.equals("EN") ? 
                        ChatColor.GREEN + "Prestige for " + target.getName() + " on " + type + " set to " + prestigeLevel :
                        ChatColor.GREEN + "Prestiż gracza " + target.getName() + " dla " + type + " ustawiony na " + prestigeLevel;
                    sender.sendMessage(msg);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Podano nieprawidłowy numer prestiżu!");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Użycie/Usage: /katana prestige set <player> <type> <number>");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("enchant")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Ta komenda może być użyta tylko przez gracza!");
                return true;
            }
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            
            KatanaEnchantmentManager enchantManager = ((KatanaModPlugin) plugin).getEnchantmentManager();
            if (enchantManager == null || !enchantManager.isKatana(item)) {
                sender.sendMessage(ChatColor.RED + "Musisz trzymać katanę w ręce!");
                return true;
            }
            
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Użycie/Usage: /katana enchant <zaklęcie/enchant> [poziom/level]");
                sender.sendMessage(ChatColor.YELLOW + "Dostępne zaklęcia/Available enchants: " + String.join(", ", enchantManager.getAvailableEnchants()));
                return true;
            }
            
            String enchantName = args[1].toLowerCase();
            if (enchantManager.getKey(enchantName) == null) {
                sender.sendMessage(ChatColor.RED + "Nieznane zaklęcie/Unknown enchant! Dostępne/Available: " + String.join(", ", enchantManager.getAvailableEnchants()));
                return true;
            }
            
            int level = 1;
            if (args.length >= 3) {
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Nieprawidłowy poziom / Invalid level!");
                    return true;
                }
            }
            
            enchantManager.applyEnchantment(item, enchantName, level);
            sender.sendMessage(ChatColor.GREEN + "Pomyślnie nałożono zaklęcie / Enchantment applied: " + enchantManager.getDisplayName(enchantName) + " " + level + "!");
            return true;
        }

        sender.sendMessage(getMsg("usage"));
        return true;
    }

    private void sendClickableLink(CommandSender sender, String url, String name) {
        sender.sendMessage(getMsg("click_link") + ChatColor.WHITE + ChatColor.UNDERLINE + url);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("katanamod.admin")) {
            if (args.length == 1) return filterStarting(Arrays.asList("github", "website"), args[0]);
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filterStarting(Arrays.asList("give", "enchant", "version", "language", "on", "off", "github", "website", "rank", "prestige"), args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) return null;
            if (args[0].equalsIgnoreCase("language")) return filterStarting(Arrays.asList("PL", "EN"), args[1]);
            if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("prestige")) return filterStarting(Collections.singletonList("set"), args[1]);
            if (args[0].equalsIgnoreCase("enchant")) {
                KatanaEnchantmentManager enchantManager = ((KatanaModPlugin) plugin).getEnchantmentManager();
                if (enchantManager != null) {
                    return filterStarting(new ArrayList<>(enchantManager.getAvailableEnchants()), args[1]);
                }
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                List<String> allTypes = new ArrayList<>(KATANA_TYPES);
                allTypes.addAll(Arrays.asList("book:kensai", "book:soulstealer", "book:windblade", "book:meditation", "book:staticshock"));
                return filterStarting(allTypes, args[2]);
            }
            if ((args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("prestige")) && args[1].equalsIgnoreCase("set")) return null; // Player
            if (args[0].equalsIgnoreCase("enchant")) {
                KatanaEnchantmentManager enchantManager = ((KatanaModPlugin) plugin).getEnchantmentManager();
                if (enchantManager != null) {
                    int maxLvl = enchantManager.getMaxLevel(args[1]);
                    List<String> lvls = new ArrayList<>();
                    for (int i = 1; i <= maxLvl; i++) {
                        lvls.add(String.valueOf(i));
                    }
                    return filterStarting(lvls, args[2]);
                }
            }
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("prestige")) && args[1].equalsIgnoreCase("set")) {
            return filterStarting(KATANA_TYPES, args[3]);
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("set")) {
                return filterStarting(Arrays.asList("WOOD", "BRONZE", "SILVER", "GOLD", "PRESTIGE"), args[4]);
            }
            if (args[0].equalsIgnoreCase("prestige") && args[1].equalsIgnoreCase("set")) {
                return filterStarting(Arrays.asList("1", "2", "3", "4", "5", "10", "100"), args[4]); // Przykładowe podpowiedzi
            }
        }
        return Collections.emptyList();
    }

    private List<String> filterStarting(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
