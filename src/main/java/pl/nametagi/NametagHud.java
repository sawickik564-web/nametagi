package pl.nametagi;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Rysuje nametagi na ekranie (widoczne przez sciany) razem z pancerzem i przedmiotami w rekach. */
public class NametagHud {

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!NametagMod.enabled || mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        float td = tickCounter.getTickDelta(true);
        TextRenderer font = mc.textRenderer;

        List<AbstractClientPlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        // dalsi pierwsi, zeby blizsi rysowali sie na wierzchu
        players.sort(Comparator.comparingDouble((AbstractClientPlayerEntity p) -> -p.squaredDistanceTo(mc.player)));

        for (AbstractClientPlayerEntity p : players) {
            if (p == mc.player || p.isSpectator()) continue;
            double dist = Math.sqrt(p.squaredDistanceTo(mc.player));
            if (dist > NametagMod.distance) continue;

            Vec3d pos = p.getLerpedPos(td).add(0, p.getHeight() + 0.45, 0);
            double[] s = project(mc, camera, pos);
            if (s == null) continue;

            drawTag(ctx, font, p, dist, (float) s[0], (float) s[1]);
        }
    }

    private static void drawTag(DrawContext ctx, TextRenderer font, PlayerEntity p, double dist, float x, float y) {
        // przedmioty: reka, pancerz (glowa -> buty), druga reka (totem)
        List<ItemStack> items = new ArrayList<>();
        add(items, p.getEquippedStack(EquipmentSlot.MAINHAND));
        add(items, p.getEquippedStack(EquipmentSlot.HEAD));
        add(items, p.getEquippedStack(EquipmentSlot.CHEST));
        add(items, p.getEquippedStack(EquipmentSlot.LEGS));
        add(items, p.getEquippedStack(EquipmentSlot.FEET));
        add(items, p.getEquippedStack(EquipmentSlot.OFFHAND));

        float hp = p.getHealth() + p.getAbsorptionAmount();
        Formatting hpColor = hp > 14 ? Formatting.GREEN : hp > 7 ? Formatting.YELLOW : Formatting.RED;
        MutableText line = Text.literal("").append(p.getDisplayName())
                .append(Text.literal(" " + Math.round(hp) + "\u2764").formatted(hpColor))
                .append(Text.literal(" " + Math.round(dist) + "m").formatted(Formatting.GRAY));
        int textW = font.getWidth(line);

        float sc = NametagMod.size;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(sc, sc, 1.0F);

        // tekst (dolna linia)
        ctx.fill(-textW / 2 - 2, -11, textW / 2 + 2, -1, 0x90000000);
        ctx.drawTextWithShadow(font, line, -textW / 2, -10, 0xFFFFFFFF);

        // rzad przedmiotow nad tekstem
        int n = items.size();
        int rowW = n * 18;
        for (int i = 0; i < n; i++) {
            int ix = -rowW / 2 + i * 18 + 1;
            int iy = -29;
            ctx.drawItem(items.get(i), ix, iy);
            ctx.drawStackOverlay(font, items.get(i), ix, iy);
        }

        ctx.getMatrices().pop();
    }

    private static void add(List<ItemStack> list, ItemStack s) {
        if (!s.isEmpty()) list.add(s);
    }

    /** Rzutuje punkt swiata na wspolrzedne ekranu GUI. Zwraca null, gdy punkt jest za kamera. */
    private static double[] project(MinecraftClient mc, Camera camera, Vec3d point) {
        Vec3d d = point.subtract(camera.getPos());
        double yaw = Math.toRadians(camera.getYaw());
        double pitch = Math.toRadians(camera.getPitch());

        double sy = Math.sin(yaw), cy = Math.cos(yaw), sp = Math.sin(pitch), cp = Math.cos(pitch);
        double fx = -sy * cp, fy = -sp, fz = cy * cp;      // przod
        double rx = -cy, ry = 0, rz = -sy;                  // prawo
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx; // gora

        double zc = d.x * fx + d.y * fy + d.z * fz;
        if (zc <= 0.05) return null;
        double xc = d.x * rx + d.y * ry + d.z * rz;
        double yc = d.x * ux + d.y * uy + d.z * uz;

        double fov = mc.options.getFov().getValue();
        double tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
        double aspect = (double) mc.getWindow().getFramebufferWidth() / mc.getWindow().getFramebufferHeight();

        double ndcX = xc / (zc * tanHalf * aspect);
        double ndcY = yc / (zc * tanHalf);

        double sx = (ndcX * 0.5 + 0.5) * mc.getWindow().getScaledWidth();
        double sy2 = (0.5 - ndcY * 0.5) * mc.getWindow().getScaledHeight();
        if (sx < -200 || sx > mc.getWindow().getScaledWidth() + 200
                || sy2 < -100 || sy2 > mc.getWindow().getScaledHeight() + 100) return null;
        return new double[]{sx, sy2};
    }
}
