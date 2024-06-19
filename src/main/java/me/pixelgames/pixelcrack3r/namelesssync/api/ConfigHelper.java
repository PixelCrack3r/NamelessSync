package me.pixelgames.pixelcrack3r.namelesssync.api;

import com.google.gson.*;
import me.pixelgames.pixelcrack3r.namelesssync.NamelessSync;

import java.io.*;
import java.util.function.Supplier;

public class ConfigHelper {

    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> T loadConfig(String fileName, Supplier<T> defaultConfig) {
        T config = null;
        try {
            File file = new File("./plugins/" + "NamelessSync" + "/" + fileName + ".json");
            if(!file.exists()) {
                if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
                file.createNewFile();
                saveConfig(fileName, defaultConfig.get());
            }
            config = (T) JsonParser.parseReader(new FileReader(file));
        } catch (IOException e) {
            NamelessSync.getInstance().getLogger().error("Could not load config file: {}", fileName, e);
        }
        return config;
    }

    public static void saveConfig(String fileName, JsonElement config) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String configString = gson.toJson(config);

            PrintWriter writer = new PrintWriter(new FileWriter("./plugins/" + "NamelessSync" + "/" + fileName + ".json"));
            writer.println(configString);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            NamelessSync.getInstance().getLogger().error("Could not save config to file: {}", fileName, e);
        }
    }

}
