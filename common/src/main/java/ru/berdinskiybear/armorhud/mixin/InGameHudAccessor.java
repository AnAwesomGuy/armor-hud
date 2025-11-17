package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
    @Accessor
    static Identifier getHOTBAR_TEXTURE() {
        throw new AssertionError();
    }

    @Accessor
    static Identifier getHOTBAR_OFFHAND_LEFT_TEXTURE() {
        throw new AssertionError();
    }

    @Invoker
    void callRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);
}
