package me.pixelgames.pixelcrack3r.namelesssync.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.pixelgames.pixelcrack3r.namelesssync.NamelessSync;
import me.pixelgames.pixelcrack3r.namelesssync.api.HttpHandler;

public class NamelessCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(args.length == 0) {
            source.sendPlainMessage("§cUsage: /nameless <command> <arguments>");
        } else if(args[0].equalsIgnoreCase("sync")) {
            source.sendPlainMessage("§7[§3NamelessSync§7] §eSyncing users...");
            HttpHandler.sync();
            source.sendPlainMessage("§7[§3NamelessSync§7] §eSuccessfully synced " + NamelessSync.getInstance().getUserCount() +  " users.");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if(invocation.arguments().length > 0)
            return invocation.source().hasPermission("namelesssync.command." + String.join(".", invocation.arguments()).toLowerCase());

        return invocation.source().hasPermission("namelesssync.command");
    }
}
