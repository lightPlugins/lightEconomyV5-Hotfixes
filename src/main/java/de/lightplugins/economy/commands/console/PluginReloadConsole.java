package de.lightplugins.economy.commands.console;

import de.lightplugins.economy.master.Main;
import de.lightplugins.economy.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class PluginReloadConsole extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin throw the console";
    }

    @Override
    public String getSyntax() {
        return "/eco reload";
    }

    @Override
    public boolean perform(Player player, String[] args) throws ExecutionException, InterruptedException {

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {

                Main.messages.reloadConfig("messages.yml");
                Main.titles.reloadConfig("titles.yml");
                Main.bankLevelMenu.reloadConfig("bank-level.yml");
                Main.bankMenu.reloadConfig("bank-menu.yml");
                Main.voucher.reloadConfig("voucher.yml");
                Main.lose.reloadConfig("lose.yml");
                Main.settings.reloadConfig("settings.yml");

                Bukkit.getConsoleSender().sendMessage(
                        Main.consolePrefix + "Successfully reloaded the configs");
                return false;
            }

            Bukkit.getConsoleSender().sendMessage( "This command does not exist. Please try /eco reload");
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(
                Main.consolePrefix + "Wrong command. Please use /eco reload");
        return false;
    }
}
