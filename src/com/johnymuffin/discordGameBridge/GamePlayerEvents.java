package com.johnymuffin.discordGameBridge;

import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GamePlayerEvents extends PlayerListener {
    private PermissionManager pex;
    private DiscordBot plugin;
    private DiscordCore dbc;
    private ConfigReader config;

    public GamePlayerEvents(DiscordBot instance, DiscordCore plugin, ConfigReader configReader) {
        this.plugin = instance;
        this.dbc = plugin;
        this.config = configReader;
        //PluginManager manager = plugin.getServer().getPluginManager();
        pex = PermissionsEx.getPermissionManager();
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        String message = config.getJoinMessage();
        trySeperate(event.getPlayer().getName());
        message = message.replaceAll("%username%", event.getPlayer().getName());
        message = message.replaceAll("%onlineCount%",
                Integer.toString(Bukkit.getServer().getOnlinePlayers().length));
        message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
        trySeperate(event.getPlayer().getName());
        dbc.Discord().DiscordSendToChannel(config.getChannel(), grabTimestamp(event.getPlayer().getWorld()) + message);

    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String message = config.getQuitMessage();
        message = message.replaceAll("%username%", event.getPlayer().getName());
        message = message.replaceAll("%onlineCount%",
                Integer.toString(Bukkit.getServer().getOnlinePlayers().length - 1));
        message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
        trySeperate(event.getPlayer().getName());
        dbc.Discord().DiscordSendToChannel(config.getChannel(), grabTimestamp(event.getPlayer().getWorld()) + message);
    }

    private String replaceMsg(String trueMsg) {
        trueMsg = trueMsg.replaceAll(Pattern.quote("@"), " ");
        trueMsg = trueMsg.replaceAll(Pattern.quote("@everyone"), " ");
        trueMsg = trueMsg.replaceAll(Pattern.quote("@here"), " ");
        // V this is stupid
        trueMsg = trueMsg.replaceAll("\\*", "\\\\\\\\*");
        trueMsg = trueMsg.replaceAll("_", "\\\\\\\\_");
        trueMsg = trueMsg.replaceAll("\\|", "\\\\\\\\|");
        trueMsg = trueMsg.replaceAll("~", "\\\\\\\\~");
        trueMsg = trueMsg.replaceAll(":", "\\\\\\\\:");
        return trueMsg;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if(!event.isCancelled()) {
            String message = config.getGameChatMessage();
            String trueMsg = replaceMsg(event.getMessage());
            String groupName = "MISSINGPEX";
            if(pex != null) {
                String pName = event.getPlayer().getName();
                groupName = pex.getUser(pName).getGroups()[0].getName();
                // [0] is always the top hierarchy group that the player has
            }

            message = message.replaceAll("%group%", groupName);
            message = message.replaceAll("%messageAuthor%", event.getPlayer().getName());
            message = message.replaceAll("%message%", trueMsg);
            trySeperate(event.getPlayer().getName());
            dbc.Discord().DiscordSendToChannel(config.getChannel(), grabTimestamp(event.getPlayer().getWorld()) + message);
        }
    }

    private String lastName = "";
    //private LocalDateTime lastTime;
    private void trySeperate(String playerName) {
        if(config.canSeparateChat()) {
            if(lastName.equals(playerName))
                return;
            dbc.Discord().DiscordSendToChannel(config.getChannel(), "​" /* zero width space */);
            lastName = playerName;
        }
    }
    private String grabTimestamp(@Nullable World world) {
        if(config.canShowTimestamp()) {
            LocalDateTime time = LocalDateTime.now();
            String timeMessage = config.getTimestampMessage();
            String minutes;
            String seconds;
            String milli;
            String ampm = "PM";
            if(config.canUseInGameTime()) {
                long tHours;
                long tMinutes;
                long tSeconds;
                long tMilli;
                if(world == null)
                    return "";
                long worldTime = world.getTime();
                tHours = worldTime / 1000 + 6;
                tMinutes = (worldTime % 1000) * 60 / 1000;
                tSeconds = (worldTime % 1000) * 60 / 1000 / 1000;
                tMilli = (worldTime % 1000) * 60 / 1000 / 1000 / 1000;
                if (tHours >= 12)
                    tHours -= 12; ampm = "AM";
                if (tHours == 0) tHours = 12;
                timeMessage = timeMessage.replaceAll("%H%", tHours+"");
                timeMessage = timeMessage.replaceAll("%h%", (tHours%12)+"");
                String mm = "0" + tMinutes;
                minutes = mm.substring(mm.length() - 2);
                seconds = String.valueOf(tSeconds);
                timeMessage = timeMessage.replaceAll("%S%", tMilli+"");
                timeMessage = timeMessage.replaceAll("%z%", "");
            } else {
                timeMessage = timeMessage.replaceAll("%h%", String.valueOf(time.getHour())); // // 12hr format
                timeMessage = timeMessage.replaceAll("%H%", time.format(DateTimeFormatter.ofPattern("H"))); // 24hr format
                minutes = String.valueOf(time.getMinute());
                if(minutes.length() < 2)
                    minutes = "0"+minutes;
                seconds = String.valueOf(time.getSecond());
                timeMessage = timeMessage.replaceAll("%S%", time.format(DateTimeFormatter.ofPattern("S"))); // milliseconds
                Calendar now = Calendar.getInstance();
                timeMessage = timeMessage.replace("%z%", "GMT"+now.getTimeZone().getRawOffset());
                ampm = time.format(DateTimeFormatter.ofPattern("a")).toUpperCase(); // AM/PM
            }
            timeMessage = timeMessage.replaceAll("%m%", minutes+"");
            timeMessage = timeMessage.replaceAll("%s%", seconds+"");
            timeMessage = timeMessage.replaceAll("%day%", time.getDayOfWeek().name());
            timeMessage = timeMessage.replaceAll("%year%", time.getYear()+"");
            timeMessage = timeMessage.replaceAll("%ampm%", ampm);
            if(timeMessage.contains("%world%")) {
                Pattern pattern = Pattern.compile("%world%(\\((.*)\\))");
                Matcher matcher = pattern.matcher(timeMessage);
                while(matcher.find()) {
                    // there's likely an easier way to do this
                    // but I'm too lazy to figure it out right now. - Ash
                    if(world != null && config.canUseInGameTime())
                        timeMessage = timeMessage.replace("%world%"+matcher.group(1), "%world%"+matcher.group(2));
                    else
                        timeMessage = timeMessage.replace("%world%"+matcher.group(1), "");
                }
                if(world != null && config.canUseInGameTime())
                    timeMessage = timeMessage.replaceAll("%world%", world.getName());
                else
                    timeMessage = timeMessage.replaceAll("%world%", "");
            }
            return timeMessage;
        }
        return "";
    }
}