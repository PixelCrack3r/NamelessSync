package me.pixelgames.pixelcrack3r.namelesssync;

import com.google.gson.*;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.pixelgames.pixelcrack3r.namelesssync.api.ConfigHelper;
import me.pixelgames.pixelcrack3r.namelesssync.api.HttpHandler;
import me.pixelgames.pixelcrack3r.namelesssync.api.User;
import me.pixelgames.pixelcrack3r.namelesssync.commands.NamelessCommand;
import me.pixelgames.pixelcrack3r.namelesssync.listeners.PlayerLoginListener;
import org.slf4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "namelesssync",
        name = "NamelessSync",
        version = "1.0-SNAPSHOT",
        authors = "PixelCrack3r"
)
public class NamelessSync {

    private static NamelessSync instance;

    private final Logger logger;
    private final ProxyServer server;

    private final Map<String, User> parsedUsers = new HashMap<>();

    @Inject
    public NamelessSync(ProxyServer server, Logger logger) {
        if(instance != null) { throw new IllegalAccessError("Cannot of two instances of NamelessSync"); }

        this.server = server;
        this.logger = logger;

        instance = this;
    }

    private JsonObject config;
    private JsonArray users;
    private JsonArray whitelist;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        this.loadConfig();
        this.loadWhitelist();
        this.loadSyncedUsers();
        this.refreshUsers();
        this.registerCommands();

        this.server.getEventManager().register(this, new PlayerLoginListener());

        if(this.getConfig().has("api-key") && !this.getConfig().get("api-key").getAsString().equals("API-KEY-HERE")) {
            this.server.getScheduler()
                    .buildTask(this, HttpHandler::sync)
                    .repeat(this.getConfig().getAsJsonObject("syncOptions").get("interval").getAsLong(), TimeUnit.MINUTES)
                    .schedule();
        }

    }

    private void registerCommands() {
        CommandMeta commandMeta = this.server.getCommandManager().metaBuilder("namelesssync")
                // This will create a new alias for the command "/test"
                // with the same arguments and functionality
                .aliases("nameless", "nls")
                .plugin(this)
                .build();

        this.server.getCommandManager().register(commandMeta, new NamelessCommand());
    }

    public JsonObject getConfig() {
        return this.config;
    }

    private void loadDefaults() {
        this.config.addProperty("enabled", true);
        this.config.addProperty("debug", false);
        this.config.addProperty("messages", false);
        this.config.addProperty("api", "API-URL-HERE");
        this.config.addProperty("api-key", "API-KEY-HERE");

        JsonObject syncOptions = new JsonObject();

        syncOptions.addProperty("interval", 60);
        syncOptions.addProperty("requiresVerification", true);
        syncOptions.addProperty("mc_integration", true);
        syncOptions.addProperty("whitelist", false);
        syncOptions.addProperty("bans", false);

        JsonObject bedrock = new JsonObject();

        bedrock.addProperty("prefix", ".");
        bedrock.addProperty("ignore", true);

        this.config.add("syncOptions", syncOptions);
        this.config.add("bedrock", bedrock);
    }

    public void loadConfig() {
        try {
            File file = new File("./plugins/" + "NamelessSync" + "/configuration.json");
            if(!file.exists()) {
                if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
                file.createNewFile();
                this.config = new JsonObject();
                this.loadDefaults();
                this.saveConfig();
            }
            this.config = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String config = gson.toJson(this.config);

            PrintWriter writer = new PrintWriter(new FileWriter("./plugins/" + "NamelessSync" + "/configuration.json"));
            writer.println(config);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSyncedUsers() {
        this.users = ConfigHelper.loadConfig("syncedUsers", JsonArray::new);
    }

    public void loadWhitelist() {
        this.whitelist = ConfigHelper.loadConfig("whitelist", JsonArray::new);
    }

    public void saveSyncedUsers() {
        ConfigHelper.saveConfig("syncedUsers", this.users);
    }

    public void setUsers(JsonArray users) {
        this.users = users;
        this.saveSyncedUsers();
        this.refreshUsers();
    }

    public void refreshUsers() {
        this.parsedUsers.clear();
        this.users.forEach(user -> {
            if(user.isJsonObject()) {
                User parsed = User.parse(user.getAsJsonObject());
                if(parsed == null) return;
                this.parsedUsers.put(parsed.getUsername(), parsed);
            }
        });
    }

    public Optional<User> getUser(String username) {
        return Optional.ofNullable(this.parsedUsers.get(username));
    }

    public int getUserCount() {
        return this.parsedUsers.size();
    }

    public Collection<String> getWhitelist() {
        return whitelist.asList().stream()
                .filter(JsonElement::isJsonPrimitive)
                .map(e -> (JsonPrimitive)e)
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString)
                .toList();
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isDebug() {
        return this.getConfig().get("debug").getAsBoolean();
    }

    public static NamelessSync getInstance() {
        return instance;
    }
}
