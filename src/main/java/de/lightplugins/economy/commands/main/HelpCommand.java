package de.lightplugins.economy.commands.main;

import de.lightplugins.economy.enums.MessagePath;
import de.lightplugins.economy.master.Main;
import de.lightplugins.economy.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class HelpCommand extends SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get an overview of all posible Commands";
    }

    @Override
    public String getSyntax() {
        return "/le help";
    }

    @Override
    public boolean perform(Player player, String[] args) {

        if(args.length == 1) {
            FileConfiguration messages = Main.messages.getConfig();
            Bukkit.getLogger().log(Level.WARNING, "INFO: " + MessagePath.Help.getPath());

            Main.util.sendMessageList(player, messages.getStringList("helpCommand"));
        }

        return false;
    }
}
