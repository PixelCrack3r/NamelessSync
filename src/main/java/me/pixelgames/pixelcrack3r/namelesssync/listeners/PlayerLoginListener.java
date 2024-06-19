package me.pixelgames.pixelcrack3r.namelesssync.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.pixelgames.pixelcrack3r.namelesssync.NamelessSync;
import me.pixelgames.pixelcrack3r.namelesssync.api.User;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class PlayerLoginListener {

    @Subscribe
    public void onLogin(LoginEvent event) {

        boolean enabled = NamelessSync.getInstance().getConfig().get("enabled").getAsBoolean();
        if(enabled) {

            boolean whitelist = NamelessSync.getInstance().getConfig().getAsJsonObject("syncOptions").get("whitelist").getAsBoolean();
            boolean syncBans = NamelessSync.getInstance().getConfig().getAsJsonObject("syncOptions").get("bans").getAsBoolean();
            boolean syncVerified = NamelessSync.getInstance().getConfig().getAsJsonObject("syncOptions").get("requiresVerification").getAsBoolean();
            boolean ignoreBedrock = NamelessSync.getInstance().getConfig().getAsJsonObject("bedrock").get("ignore").getAsBoolean();

            Player player = event.getPlayer();
            if(player.hasPermission("namelesssync.join")) return;
            if(NamelessSync.getInstance().getWhitelist().contains(player.getUsername())) return;

            if(ignoreBedrock && player.getUsername().startsWith(NamelessSync.getInstance().getConfig().getAsJsonObject("bedrock").get("prefix").getAsString())) {
                return;
            }

            Optional<User> user = NamelessSync.getInstance().getUser(player.getUsername());
            if(whitelist && user.isEmpty()) {
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYou are not registered on this server.\nPlease contact an admin or register on our website!")));
                return;
            } else if(user.isPresent()) {
                User namelessUser = user.get();
                if(syncBans && namelessUser.isBanned()) {
                    event.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYou are banned from this server!")));
                    return;
                } else if(syncVerified && !namelessUser.isVerified()) {
                    event.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYou are not verified! Please contact an admin or verify on our website!")));
                    return;
                }
            }
        }

    }

}
