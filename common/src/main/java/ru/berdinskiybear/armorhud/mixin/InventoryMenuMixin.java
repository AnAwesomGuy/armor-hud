package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InventoryMenu.class)
public interface InventoryMenuMixin {
    @Accessor
    static Map<EquipmentSlot, ResourceLocation> getTEXTURE_EMPTY_SLOTS() {
        throw new AssertionError();
    }

    @Accessor
    static EquipmentSlot[] getSLOT_IDS() {
        throw new AssertionError();
    }
}
