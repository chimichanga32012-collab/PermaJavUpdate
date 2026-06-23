package tech.sebazcrc.permadeath;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PDCCommandCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                    "dias", "duracion", "awake", "info", "discord", "mensaje", "idioma", "cambios"
            ));

            if (sender.hasPermission("permadeathcore.cambiardia")) {
                subcommands.add("cambiardia");
            }
            if (sender.hasPermission("permadeathcore.reload")) {
                subcommands.add("reload");
            }
            if (sender.hasPermission("permadeathcore.give")) {
                subcommands.add("give");
            }
            if (sender.hasPermission("permadeathcore.locate")) {
                subcommands.add("locate");
            }
            if (sender.hasPermission("permadeathcore.event")) {
                subcommands.add("event");
            }
            if (sender.hasPermission("permadeathcore.admin")) {
                subcommands.add("storm");
                subcommands.add("afk");
                subcommands.add("debug");
                subcommands.add("speedrun");
                subcommands.add("beginning");
            }
            
            // Filtra la lista según lo que ya escribió el usuario
            return StringUtil.copyPartialMatches(args[0], subcommands, completions);
        }

        if (args.length == 2) {
            String firstArg = args[0].toLowerCase(); // El primer argumento real está en el índice 0
            List<String> options = new ArrayList<>();

            switch (firstArg) {
                case "give":
                    if (sender.hasPermission("permadeathcore.give")) {
                        options.addAll(Arrays.asList("netheriteArmor", "infernalArmor", "medalla", "netheriteTools", "infernalBlock", "lifeOrb", "endRelic", "beginningRelic"));
                    }
                    break;
                case "locate":
                    if (sender.hasPermission("permadeathcore.locate")) {
                        options.add("beginning");
                    }
                    break;
                case "idioma":
                    options.addAll(Arrays.asList("es", "en"));
                    break;
                case "event":
                    if (sender.hasPermission("permadeathcore.event")) {
                        options.addAll(Arrays.asList("shulkershell", "lifeorb"));
                    }
                    break;
                case "afk":
                    if (sender.hasPermission("permadeathcore.admin")) {
                        options.addAll(Arrays.asList("unban", "bypass"));
                    }
                    break;
                case "storm":
                    if (sender.hasPermission("permadeathcore.admin")) {
                        options.addAll(Arrays.asList("addHours", "removeHours"));
                    }
                    break;
                case "debug":
                    if (sender.hasPermission("permadeathcore.admin")) {
                        options.addAll(Arrays.asList("info", "toggle", "emptyworld", "module", "health", "events", "hasorb", "hyper", "removegaps", "showhealthskeleton", "withertime", "testtotems", "testtotemsb", "testlingering", "summonske", "testwither", "spawncreeper", "addtimespeedrun", "muerte"));
                    }
                    break;
                case "speedrun":
                    if (sender.hasPermission("permadeathcore.admin")) {
                        options.addAll(Arrays.asList("toggle", "tiempo", "reset"));
                    }
                    break;
                case "beginning":
                    if (sender.hasPermission("permadeathcore.admin")) {
                        options.addAll(Arrays.asList("bendicion", "maldicion"));
                    }
                    break;
                case "cambiardia":
                    if (sender.hasPermission("permadeathcore.cambiardia")) {
                        options.add("<día>");
                    }
                    break;
            }

            return StringUtil.copyPartialMatches(args[1], options, completions);
        }

        // Tercer argumento para comandos específicos como /pdc afk bypass <add/remove>
        if (args.length == 3) {
            String firstArg = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            List<String> options = new ArrayList<>();

            if (firstArg.equals("afk") && secondArg.equals("bypass") && sender.hasPermission("permadeathcore.admin")) {
                options.addAll(Arrays.asList("add", "remove"));
            }

            return StringUtil.copyPartialMatches(args[2], options, completions);
        }

        return Collections.emptyList();
    }
}
