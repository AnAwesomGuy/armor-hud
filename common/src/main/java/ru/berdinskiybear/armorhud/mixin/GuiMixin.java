package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int tickCount;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    public void renderArmorHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled()) return;

        Player player = getCameraPlayer();
        if (player == null) return;

        ArmorHudMod.render((GuiAccessor)this, context, tickCounter, player, minecraft, tickCount);
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
            || config.getSide() != HumanoidArm.RIGHT) return;

        Player player = this.getCameraPlayer();
        if (player == null) return;

        shiftRef.set(Math.max(ArmorHudMod.getArmorHudOffset(player, config, 9, 8, ArmorHudMod.SIZE), 0));
    }

    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "CONSTANT", args = "intValue=1"))
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}