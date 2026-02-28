package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

@Mixin(SubtitleOverlay.class)
public class SubtiteOverlayMixin {
    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle (i take it back i understand this now)
    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I", ordinal = 3))
    public void calculateOffset(GuiGraphics context, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM ||
            config.getSide() != HumanoidArm.RIGHT) return;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        offsetRef.set(Math.max(ArmorHudMod.getArmorHudOffset(player, config, 4, 1, 0), 0));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 1)
    public float offset(float y, @Share("offset") LocalIntRef offsetRef) {
        return y - offsetRef.get();
    }
}
