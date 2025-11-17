package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int ticks;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "renderHotbarVanilla", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled()) return;

        PlayerEntity player = getCameraPlayer();
        if (player == null) return;

        ArmorHudMod.render((InGameHudAccessor)this, context, tickCounter, player, client, ticks);
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
            || config.getSide() != Arm.RIGHT) return;

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        List<ItemStack> armor = ArmorHudMod.nonEmptyArmor(player);
        if (armor.isEmpty() || config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        int newShift = ArmorHudMod.SIZE + config.getOffsetY();
        if (config.isWarningShown() && armor.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
            newShift += 10;
            if (config.getWarningBobIntensity() != 0)
                newShift += ArmorHudMod.WARNING_OFFSET;
        }

        shiftRef.set(Math.max(newShift, 0));
    }

    @ModifyExpressionValue(method = "renderStatusEffectOverlay", at = @At(value = "CONSTANT", args = "intValue=1"))
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}