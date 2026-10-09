package pl.nametagi.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.nametagi.NametagMod;

// Chowa zwykle nametagi innych graczy, bo mod rysuje wlasne (z pancerzem i przedmiotami).
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void nametagi$hasLabel(LivingEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!NametagMod.enabled || !(entity instanceof PlayerEntity)) return;
        if (entity == MinecraftClient.getInstance().player) return;
        cir.setReturnValue(false);
    }
}
