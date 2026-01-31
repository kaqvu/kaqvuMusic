package pl.kaqvu.music;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class kaqvuMusicClient implements ClientModInitializer {
    public static final String mod_id = "kaqvumusic";

    @Override
    public void onInitializeClient() {
        MusicManager.init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            MusicCommand.register(dispatcher);
        });
    }
}
