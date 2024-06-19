package me.pixelgames.pixelcrack3r.namelesssync.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.pixelgames.pixelcrack3r.namelesssync.NamelessSync;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class User {

    private final int id;
    private final String username;
    private final String uuid;
    private final boolean banned;
    private final boolean verified;

    public User(int id, String username, String uuid, boolean banned, boolean verified) {
        this.id = id;
        this.username = username;
        this.uuid = uuid;
        this.banned = banned;
        this.verified = verified;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isVerified() {
        return verified;
    }

    public static User parse(JsonObject json) {
        boolean useMc = NamelessSync.getInstance().getConfig().getAsJsonObject("syncOptions").get("mc_integration").getAsBoolean();

        JsonArray integrations = json.has("integrations") ? json.getAsJsonArray("integrations") : null;

        AtomicReference<JsonObject> minecraft = new AtomicReference<>();
        if (useMc) {
            if (integrations == null) return null;

            integrations.forEach(integration -> {
                if (integration.isJsonObject() && integration.getAsJsonObject().get("integration").getAsString().equalsIgnoreCase("minecraft")) {
                    minecraft.set(integration.getAsJsonObject());
                }
            });
            if (minecraft.get() == null) return null;
            if (!minecraft.get().has("identifier")) return null;
            if (!minecraft.get().has("username")) return null;

        }

        final String uuid = useMc ? minecraft.get().get("identifier").getAsString() : "";

        final int id = json.get("id").getAsInt();
        final String username = useMc ? minecraft.get().get("username").getAsString() : json.get("username").getAsString();
        final boolean banned = json.get("banned").getAsBoolean();
        final boolean verified = json.get("verified").getAsBoolean();
        return new User(id, username, uuid, banned, verified);
    }
}
