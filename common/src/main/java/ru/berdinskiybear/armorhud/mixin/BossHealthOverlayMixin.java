package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "intValue=12"))
    public int pushBossBars(int y) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return y;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return y;

        final int orig = y;
        List<ItemStack> armorItems = ArmorHudMod.nonEmptyArmor(player);

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            y += ArmorHudMod.SIZE + config.getOffsetY();
            if (y > orig && config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                y += 10;
                if (config.getWarningBobIntensity() != 0)
                    y += ArmorHudMod.WARNING_OFFSET;
            }
        }

        return Math.max(y, orig);
    }
}
