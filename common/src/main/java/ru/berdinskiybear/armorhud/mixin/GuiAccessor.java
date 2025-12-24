package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor
    static ResourceLocation getHOTBAR_SPRITE() {
        throw new AssertionError();
    }

    @Accessor
    static ResourceLocation getHOTBAR_OFFHAND_LEFT_SPRITE() {
        throw new AssertionError();
    }

    @Invoker
    void callRenderSlot(GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed);
}
