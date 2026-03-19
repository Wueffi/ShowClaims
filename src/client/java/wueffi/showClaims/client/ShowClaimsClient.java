package wueffi.showClaims.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowClaimsClient implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger("ShowClaims");
    private KeyBinding toggleKey;
    public static boolean visible = true;
    public static ClaimConfig config;
    private boolean configLoaded = false;

    @Override
    public void onInitializeClient() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("ShowClaims initializing...");

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.regionhud.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.regionhud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                visible = !visible;
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(new HUDRenderer());

        LOGGER.info("Done initializing. Took " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null && configLoaded) configLoaded = false;
        if (configLoaded || client.player == null) return;
        configLoaded = true;
        config = ConfigLoader.load(client.player);
    }
}
