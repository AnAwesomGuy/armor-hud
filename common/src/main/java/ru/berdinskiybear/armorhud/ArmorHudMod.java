package ru.berdinskiybear.armorhud;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.nio.file.Path;
import java.util.List;

public final class ArmorHudMod {
    public static final String MOD_ID = "ukus_armor_hud";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyBinding TOGGLE_HUD = new KeyBinding("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN,
                                                               "armorhud.name");

    public static final Identifier WARNING_TEXTURE = Identifier.of(ArmorHudMod.MOD_ID, "warn.png");

    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
    }

    public static List<ItemStack> nonEmptyArmor(PlayerEntity player) {
        return player.getInventory().armor.stream().filter(s -> !s.isEmpty()).toList();
    }

    public static void renderDurabilityNumbers(TextRenderer textRenderer, ItemStack stack, int x, int y, DrawContext context) {
        if (ArmorHudConfig.CONFIG.getDurabilityStyle() == ArmorHudConfig.DurabilityStyle.NUMBERS) { // render durability numbers
            int durability = stack.getMaxDamage() - stack.getDamage();
            String s = String.valueOf(durability);
            int width = textRenderer.getWidth(s);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            float factor = 16F / width;
            if (factor > 1F) {
                factor = 1F;
                x += (16 - width) / 2;
                x = (int)(x / factor);
            } else
                x = (int)(x / factor) + 1;
            y = (int)((y - 3) / factor) - 10;
            matrices.scale(factor, factor, 0F);
            context.drawText(textRenderer, s, x, y, stack.getItemBarColor(), true);
            matrices.pop();
        }
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return false;

        final int damage = stack.getDamage();
        final int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double)damage / maxDamage);

        return percentage <= ArmorHudConfig.CONFIG.getMinDurabilityPercentage()
            || maxDamage - damage <= ArmorHudConfig.CONFIG.getMinDurabilityValue();
    }

    @ExpectPlatform
    public static Path configDir() {
        throw new AssertionError();
    }
}
