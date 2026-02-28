package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.BossHealthOverlay;
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

        return y + Math.max(ArmorHudMod.getArmorHudOffset(player, config, 9, 8, ArmorHudMod.SIZE), 0);
    }
}
