package pl.nametagi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NametagMod implements ClientModInitializer {
    public static boolean enabled = true;
    public static int distance = 64; // w blokach

    private static KeyBinding toggleKey, plusKey, minusKey;

    @Override
    public void onInitializeClient() {
        String cat = "key.categories.misc";
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nametagi: wlacz/wylacz", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, cat));
        plusKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nametagi: zasieg +", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, cat));
        minusKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nametagi: zasieg -", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, cat));

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
        });
    }

    private static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }
}
