package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "intValue=12"))
    public int pushBossBars(int y) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return y;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return y;

        TriState warnings = ArmorHudMod.showWarningsInHud(player, config);
        if (warnings == TriState.DEFAULT) return y;

        int orig = y;
        y += ArmorHudMod.SIZE + config.getOffsetY();
        if (warnings == TriState.TRUE)
            y += config.getWarningBobIntensity() + 8;

        return Math.max(y, orig);
    }
}
