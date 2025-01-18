package me.pixelgames.pixelcrack3r.namelesssync.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pixelgames.pixelcrack3r.namelesssync.NamelessSync;

import java.net.URL;

public class HttpHandler {

    public static void sync() {
        try {
            if(NamelessSync.getInstance().getConfig().get("messages").getAsBoolean()) NamelessSync.getInstance().getLogger().info("Syncing users...");

            HttpRequest request = new HttpRequest();

            request.setUrl(new URL(NamelessSync.getInstance().getConfig().get("api").getAsString() + "/users?limit=0"));
            request.setMethod("GET");
            request.setProperty("Authorization", "Bearer " + NamelessSync.getInstance().getConfig().get("api-key").getAsString());

            request.doRequest();

            if(NamelessSync.getInstance().isDebug())
                NamelessSync.getInstance().getLogger().info("{} ({}): {}", NamelessSync.getInstance().getConfig().get("api").getAsString(), request.getResponseCode(), request.getResponse());

            if(!Integer.toString(request.getResponseCode()).startsWith("2")) {
                NamelessSync.getInstance().getLogger().error("Could not sync with {}:\n{}", NamelessSync.getInstance().getConfig().get("api"), request.getResponse());
                return;
            }

            JsonObject response = JsonParser.parseString(request.getResponse()).getAsJsonObject();
            if(response.has("users")) {
                JsonArray users = response.getAsJsonArray("users");
                NamelessSync.getInstance().setUsers(users);
                if(NamelessSync.getInstance().getConfig().get("messages").getAsBoolean()) NamelessSync.getInstance().getLogger().info("Successfully synced {} users.", NamelessSync.getInstance().getUserCount());
                return;
            }

            NamelessSync.getInstance().getLogger().warn("Response does not contain a users list!");

        } catch (Exception e) {
            NamelessSync.getInstance().getLogger().error("Could not sync with {}", NamelessSync.getInstance().getConfig().get("api"), e);
        }

    }

}
