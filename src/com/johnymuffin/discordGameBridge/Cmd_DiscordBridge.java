package com.johnymuffin.discordGameBridge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Cmd_DiscordBridge implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length > 0) {
            if(strings[0].equalsIgnoreCase("reload")) {
                DiscordBot.getInstance().configReader.reload();
            }
        } else {
            commandSender.sendMessage("/DiscordBridge reload");
        }
        return false;
    }
}
