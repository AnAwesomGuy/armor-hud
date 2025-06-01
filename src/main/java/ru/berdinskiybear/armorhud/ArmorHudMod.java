package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

public final class ArmorHudMod implements ClientModInitializer {
    public static final String MOD_ID = "ukus-armor-hud";

    public static final Identifier WARNING_TEXTURE = Identifier.of(MOD_ID, "warn.png");

    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, MOD_ID);

    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
    }

    public static List<ItemStack> nonEmptyArmor(PlayerEntity player) {
        return player.getInventory().armor.stream().filter(s -> !s.isEmpty()).toList();
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return false;

        final int damage = stack.getDamage();
        final int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double) damage / maxDamage);

        return percentage <= manager.getConfig().getMinDurabilityPercentage()
                || maxDamage - damage <= manager.getConfig().getMinDurabilityValue();
    }

    @Override
    public void onInitializeClient() {
        Ukutils.registerToggleBind(new KeyBinding("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN, "armorhud.name"),
                () -> manager.getConfig().isEnabled(), b -> manager.getConfig().setEnabled(b), Text.translatable("armorhud.keybind.toggle.msg"));
    }
}
