package pl.nametagi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NametagMod implements ClientModInitializer {
    public static boolean enabled = true;
    public static int distance = 64;   // zasieg w blokach
    public static float size = 1.0F;   // wielkosc nametaga

    private static KeyBinding toggleKey, plusKey, minusKey, sizeUpKey, sizeDownKey;

    @Override
    public void onInitializeClient() {
        String cat = "key.categories.misc";
        toggleKey = reg("Nametagi: wlacz/wylacz", GLFW.GLFW_KEY_N, cat);
        plusKey = reg("Nametagi: zasieg +", GLFW.GLFW_KEY_RIGHT_BRACKET, cat);
        minusKey = reg("Nametagi: zasieg -", GLFW.GLFW_KEY_LEFT_BRACKET, cat);
        sizeUpKey = reg("Nametagi: wielkosc +", GLFW.GLFW_KEY_PERIOD, cat);
        sizeDownKey = reg("Nametagi: wielkosc -", GLFW.GLFW_KEY_COMMA, cat);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                msg(mc, "Nametagi: " + (enabled ? "ON" : "OFF"));
            }
            while (plusKey.wasPressed()) {
                distance = Math.min(512, distance + 8);
                msg(mc, "Zasieg nametagow: " + distance + " blokow");
            }
            while (minusKey.wasPressed()) {
                distance = Math.max(8, distance - 8);
                msg(mc, "Zasieg nametagow: " + distance + " blokow");
            }
            while (sizeUpKey.wasPressed()) {
                size = Math.min(4.0F, size + 0.25F);
                msg(mc, "Wielkosc nametagow: " + size + "x");
            }
            while (sizeDownKey.wasPressed()) {
                size = Math.max(0.5F, size - 0.25F);
                msg(mc, "Wielkosc nametagow: " + size + "x");
            }
        });

        HudRenderCallback.EVENT.register(NametagHud::render);
    }

    private static KeyBinding reg(String name, int key, String cat) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, cat));
    }

    private static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }
}
