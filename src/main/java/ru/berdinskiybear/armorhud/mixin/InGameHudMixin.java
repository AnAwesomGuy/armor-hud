package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

import static ru.berdinskiybear.armorhud.mixin.Constants.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private static Identifier HOTBAR_TEXTURE;

    @Shadow
    @Final
    private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private Random random;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Profilers.get().push("ukus-armor-hud");

        // this was extracted to a different method to be able to return whenever I want
        // without messing up the profiler
        drawArmorHud(context, tickCounter);

        // pop this out of profiler
        Profilers.get().pop();
    }

    @Unique
    private void drawArmorHud(DrawContext context, RenderTickCounter tickCounter) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled()) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        // fetch armor items
        List<ItemStack> armor = player.getInventory().armor;
        // amount is always in [0,4], we can safely cast to int
        int nonEmptyAmount = (int) armor.stream().filter(s -> !s.isEmpty()).count();

        // return if there is nothing to draw
        if (nonEmptyAmount == 0 && config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        if (config.isReversed()) {
            armor = armor.reversed();
        }

        // push them matrices :3
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);

        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final int sideMultiplier, sideOffsetMultiplier;
        if ((config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.LEFT)
                || (config.getAnchor() != ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.RIGHT)) {
            sideMultiplier = -1;
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1;
            sideOffsetMultiplier = 0;
        }

        final int verticalMultiplier = switch (config.getAnchor()) {
            case TOP, TOP_CENTER -> 1;
            case BOTTOM, HOTBAR -> -1;
        };

        final int verticalOffsetMultiplier = switch (config.getAnchor()) {
            case TOP, TOP_CENTER -> 0;
            case BOTTOM, HOTBAR -> -1;
        };

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
            case ALWAYS_LEAVE_SPACE -> Math.max(OFFHAND_OFFSET, ATTACK_INDICATOR_OFFSET);
            case ADHERE -> {
                if (player.getMainArm().getOpposite() == config.getSide().asArm()) {
                    if (!player.getOffHandStack().isEmpty()) {
                        yield OFFHAND_OFFSET;
                    } else if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                        yield ATTACK_INDICATOR_OFFSET;
                    }
                }

                yield 0;
            }
        };

        final int slots = config.getWidgetShown() == ArmorHudConfig.WidgetShown.NOT_EMPTY ? nonEmptyAmount : 4;
        final int widgetWidth = SIZE + ((slots - 1) * STEP);

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> context.getScaledWindowWidth() / 2 - (widgetWidth / 2);
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR ->
                    context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
        };

        final int armorWidgetY = config.getOffsetY() * verticalMultiplier + switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> context.getScaledWindowHeight() - SIZE;
            case TOP, TOP_CENTER -> 0;
        };

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // here I draw the slots
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, -91);
        switch (config.getStyle()) {
            case HOTBAR -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, widgetWidth - 3, SIZE);
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, SIZE);
            }
            case ROUNDED_CORNERS -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, 3, SIZE);
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_TEXTURE, 182, 22, 3, 0, armorWidgetX + 3, armorWidgetY, widgetWidth - 6, SIZE);
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, SIZE - 3, 1, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, SIZE);
            }
            case ROUNDED -> {
                int borderWidth = (SIZE - STEP) / 2;
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, borderWidth, SIZE);
                for (int i = 0; i < slots; i++) {
                    context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, borderWidth, 1, armorWidgetX + borderWidth + i * STEP, armorWidgetY, STEP, SIZE);
                }
                context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX + widgetWidth - borderWidth, armorWidgetY, borderWidth, SIZE);
            }
        }
        context.getMatrices().pop();

        // here I draw warning icons if necessary
        if (config.isWarningShown()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 90);

            int i = 0;
            for (ItemStack stack : armor) {
                if (ArmorHudMod.shouldShowWarning(stack)) {
                    int x = armorWidgetX + (STEP * i) + WARNING_OFFSET;
                    int y = armorWidgetY + (SIZE * (verticalOffsetMultiplier + 1)) + (8 * verticalOffsetMultiplier);

                    if (config.getWarningBobIntensity() != 0) {
                        int intensity = config.getWarningBobIntensity();
                        y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
                    }

                    context.drawTexture(RenderLayer::getGuiTextured, ArmorHudMod.WARNING_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);
                    i++;
                } else if (config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY || !stack.isEmpty()) {
                    i++;
                }
            }

            context.getMatrices().pop();
        }

        // here I blend in slot icons if so tells the current config
        if (config.isIconsShown() && config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, -90);
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

            for (int i = 0; i < armor.size(); i++) {
                if (armor.get(i).isEmpty()) {
                    int slotIndex = config.isReversed() ? i : 3 - i;
                    Identifier texture = PlayerScreenHandlerAccessor.getEMPTY_ARMOR_SLOT_TEXTURES().get(PlayerScreenHandlerAccessor.getEQUIPMENT_SLOT_ORDER()[slotIndex]);
                    context.drawGuiTexture(RenderLayer::getGuiTextured, texture, armorWidgetX + (STEP * i) + 3, armorWidgetY + 3, 16, 16);
                }
            }

            RenderSystem.defaultBlendFunc();
            context.getMatrices().pop();
        }

        // and at last I draw the armour items
        int i = 0;
        for (ItemStack stack : armor) {
            if (!stack.isEmpty()) {
                this.renderHotbarItem(context, armorWidgetX + (STEP * i) + 3, armorWidgetY + 3, tickCounter, player, stack, i + 1);
            }

            if (!stack.isEmpty() || config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY) {
                i++;
            }
        }

        // remove my translations
        context.getMatrices().pop();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        List<ItemStack> armor = ArmorHudMod.nonEmptyArmor(player);
        if (armor.isEmpty() || config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        int newShift = 22 + config.getOffsetY();
        if (config.isWarningShown() && armor.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
            newShift += 10;
            if (config.getWarningBobIntensity() != 0) {
                newShift += 7;
            }
        }

        shiftRef.set(Math.max(newShift, 0));
    }

    @ModifyExpressionValue(method = "renderStatusEffectOverlay", at = @At(value = "CONSTANT", args = "intValue=1"))
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}
