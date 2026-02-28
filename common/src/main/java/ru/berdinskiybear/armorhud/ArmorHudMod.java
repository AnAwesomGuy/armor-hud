package ru.berdinskiybear.armorhud;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import ru.berdinskiybear.armorhud.mixin.GuiAccessor;
import ru.berdinskiybear.armorhud.mixin.InventoryMenuAccessor;

import java.nio.file.Path;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
import static ru.berdinskiybear.armorhud.mixin.GuiAccessor.getHOTBAR_OFFHAND_LEFT_SPRITE;
import static ru.berdinskiybear.armorhud.mixin.GuiAccessor.getHOTBAR_SPRITE;

public final class ArmorHudMod {
    public static final String MOD_ID = "armor_hud";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final EquipmentSlot[] SLOT_IDS = InventoryMenuAccessor.getSLOT_IDS();

    public static final KeyMapping TOGGLE_HUD = new KeyMapping("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN,
                                                               new KeyMapping.Category(
                                                                   Identifier.fromNamespaceAndPath(ArmorHudMod.MOD_ID,
                                                                                                   "keybinds")));


    public static final Identifier WARNING_TEXTURE = Identifier.fromNamespaceAndPath(ArmorHudMod.MOD_ID, "warn");

    public static final int STEP = 20, SIZE = 22, EDGE_SIZE = 3,
        HOTBAR_OFFSET = 98, OFFHAND_OFFSET = SIZE + 7, ATTACK_INDICATOR_OFFSET = 23, WARNING_OFFSET = 4; // constants

    @Nullable
    public static Player getCameraPlayer() {
        return Minecraft.getInstance().getCameraEntity() instanceof Player player ? player : null;
    }

    // returns: true -> show warnings, false -> no warnings but hud is shown, default -> hud isn't shown at all
    public static TriState showWarningsInHud(Player player, ArmorHudConfig config) {
        boolean hasItems = config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS;
        final boolean showWarnings = config.isWarningShown();
        for (EquipmentSlot slot : SLOT_IDS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                if (showWarnings) {
                    if (shouldShowWarning(stack))
                        return TriState.TRUE;
                    // hasItems = true
                } else
                    return TriState.FALSE;
                hasItems = true;
            }
        }
        return hasItems ? TriState.FALSE : TriState.DEFAULT;
    }

    public static int nonEmptyArmorCount(Player player) {
        int count = 0;
        for (EquipmentSlot slot : SLOT_IDS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty())
                count++;
        }
        return count;
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;

        final int damage = stack.getDamageValue();
        final int maxDamage = stack.getMaxDamage();
        return maxDamage - damage <= ArmorHudConfig.CONFIG.getMinDurabilityValue() ||
            100 - ((100 * damage) / maxDamage) <= ArmorHudConfig.CONFIG.getMinDurabilityPercentage();
    }

    // message me on discord if you need help reading this, i can try to explain (or ask ai or smth idk)
    public static void render(GuiAccessor gui, GuiGraphics context, DeltaTracker tickCounter, Player player, Minecraft client, int ticks) {
        final ArmorHudConfig config = ArmorHudConfig.CONFIG;
        final int nonEmptyCount = nonEmptyArmorCount(player);

        // return if there is nothing to draw
        if (nonEmptyCount == 0 && config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS)
            return;

        final Matrix3x2fStack matrices = context.pose();
        final ArmorHudConfig.Anchor anchor = config.getAnchor();
        final boolean anchorTop = anchor.isTop();
        final boolean right = config.getSide() == HumanoidArm.RIGHT;
        boolean vertical = config.isVertical();
        final boolean showEmpty = config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY;
        final int slots = showEmpty ? 4 : nonEmptyCount;
        final int widgetSize = SIZE + (slots - 1) * STEP;

        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final int sideMultiplier, sideOffsetMultiplier;
        // (anchor == ArmorHudConfig.Anchor.HOTBAR && !right) || (anchor != ArmorHudConfig.Anchor.HOTBAR && right)
        if ((anchor == ArmorHudConfig.Anchor.HOTBAR) != right) {
            sideMultiplier = -1; // right or hotbar
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1; // left
            sideOffsetMultiplier = 0;
        }

        int widgetX = config.getOffsetX() * sideMultiplier;
        if (anchor == ArmorHudConfig.Anchor.TOP_CENTER) {
            widgetX += (context.guiWidth() - widgetSize) / 2;
            if (vertical) {
                config.setVertical(vertical = false);
                LOGGER.warn("Disabling vertical mode because the top center anchor is incompatible!");
            }
        } else if (anchor == ArmorHudConfig.Anchor.HOTBAR) {
            final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
                case ALWAYS_IGNORE -> 0;
                case ALWAYS_LEAVE_SPACE -> OFFHAND_OFFSET;
                case ADHERE -> {
                    if (player.getMainArm().getOpposite() == config.getSide())
                        if (!player.getOffhandItem().isEmpty())
                            yield OFFHAND_OFFSET;
                        else if (client.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR)
                            yield ATTACK_INDICATOR_OFFSET;
                    yield 0;
                }
            };
            widgetX += context.guiWidth() / 2 + (HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier + widgetSize * sideOffsetMultiplier;
            if (vertical) {
                config.setVertical(vertical = false);
                LOGGER.warn("Disabling vertical mode because the hotbar anchor is incompatible!");
            }
        } else if (vertical)
            widgetX += (SIZE - context.guiWidth()) * sideOffsetMultiplier;
        else if (anchor == ArmorHudConfig.Anchor.TOP || anchor == ArmorHudConfig.Anchor.BOTTOM)
            widgetX += (widgetSize - context.guiWidth()) * sideOffsetMultiplier;

        final int widgetY = anchorTop ? config.getOffsetY() : context.guiHeight() - config.getOffsetY() - (vertical ? widgetSize : SIZE);

        final int rotatedY, rotatedX;
        if (vertical) { // adjust for vertical
            matrices.pushMatrix();
            matrices.rotate(Mth.DEG_TO_RAD * 90F); // rotate 90deg to vertical
            // here i "swap" the x and the y in order to have the correct position
            // noinspection SuspiciousNameCombination (yes ik it's strange)
            rotatedX = widgetY;
            rotatedY = -widgetX - SIZE;
        } else {
            rotatedX = widgetX;
            rotatedY = widgetY;
        }

        // here I draw the slots (help me)
        // blitSprite(RenderPipeline pipeline, Identifier texture, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height)
        // blitSprite(RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height)
        // 182 and 22 is the width and height of the hotbar texture
        // 29 and 24 is the width and height of the offhand texture
        switch (config.getStyle()) {
            case HOTBAR -> {
                context.blitSprite(GUI_TEXTURED, getHOTBAR_SPRITE(), 182, 22, 0, 0, rotatedX, rotatedY,
                                   widgetSize - EDGE_SIZE, SIZE); // main part (left)
                context.blitSprite(GUI_TEXTURED, getHOTBAR_SPRITE(), 182, 22, 182 - EDGE_SIZE, 0,
                                   rotatedX + widgetSize - EDGE_SIZE, rotatedY, EDGE_SIZE, SIZE); // right edge
            }
            case ROUNDED_CORNERS -> {
                if (slots > 1) {
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 0, 1,
                                       rotatedX, rotatedY, EDGE_SIZE, SIZE); // round left edge
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_SPRITE(), 182, 22, EDGE_SIZE, 0,
                                       rotatedX + EDGE_SIZE, rotatedY, widgetSize - 6, SIZE); // middle
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, SIZE - EDGE_SIZE, 1,
                                       rotatedX + widgetSize - EDGE_SIZE, rotatedY, EDGE_SIZE,
                                       SIZE); // round right edge
                } else // only one round slot
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 0, 1,
                                       rotatedX, rotatedY, SIZE, SIZE);
            }
            case ROUNDED -> {
                if (slots > 1) {
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 0, 1,
                                       rotatedX, rotatedY, SIZE - 1, SIZE); // left slot
                    for (int i = slots - 2; i >= 1; i--) // nothing happens if slots <= 2
                        context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 1, 1,
                                           rotatedX + 1 + i * STEP, rotatedY, STEP, SIZE); // middle slots
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 1, 1,
                                       rotatedX + widgetSize - STEP - 1, rotatedY, SIZE - 1, SIZE); // right slot
                } else // only one round slot
                    context.blitSprite(GUI_TEXTURED, getHOTBAR_OFFHAND_LEFT_SPRITE(), 29, 24, 0, 1,
                                       rotatedX, rotatedY, SIZE, SIZE);
            }
            // case NONE -> (nothing!)
        }

        if (vertical)
            matrices.popMatrix(); // pop the rotation

        // calculate warning offset
        int warningOffset = 0;
        if (config.isWarningShown()) {
            final int intensity = config.getWarningBobIntensity();
            // warningOffset = vertical ? (right ? -12 : STEP) : (anchorTop ? STEP : -12);
            warningOffset = (vertical && !right || !vertical && anchorTop) ? STEP : -8 - WARNING_OFFSET;
            if (intensity != 0) {
                // sine wave that goes up and down for the bob
                int bob = Math.round(Mth.sin(ticks / 2F) / 2F * intensity); // hi bob
                // invert bob if vertical and on the right or if anchored on the top
                warningOffset += vertical ? bob * sideMultiplier : (anchorTop ? -bob : bob);
            }
        }

        // draw the armour items and the warning signs if necessary
        final boolean drawBackground = showEmpty && config.isIconsShown();
        final ArmorHudConfig.DurabilityDisplay durabilityDisplay = config.getDurabilityDisplay();
        for (int i = 0, x = widgetX + EDGE_SIZE, y = widgetY + EDGE_SIZE; i < 4; i++) {
            int index = config.isReversed() ? 4 - i - 1 : i;
            ItemStack stack = player.getItemBySlot(SLOT_IDS[index]);
            if (!stack.isEmpty()) {
                // draw item
                gui.callRenderSlot(context, x, y, tickCounter, player, stack, nonEmptyCount);

                // draw warning (behind durability numbers)
                if (config.isWarningShown() && ArmorHudMod.shouldShowWarning(stack))
                    context.blitSprite(GUI_TEXTURED, WARNING_TEXTURE,
                                       x + (vertical ? warningOffset : WARNING_OFFSET),
                                       y + (vertical ? WARNING_OFFSET : warningOffset),
                                       8, 8);

                // render durability numbers
                if (durabilityDisplay != ArmorHudConfig.DurabilityDisplay.BAR && stack.isDamageableItem()) {
                    Font textRenderer = client.font;
                    String s = switch (durabilityDisplay) {
                        case NUMERIC -> String.valueOf(stack.getMaxDamage() - stack.getDamageValue());
                        // truncates
                        case PERCENTAGE -> (100 - (stack.getDamageValue() * 100) / stack.getMaxDamage()) + "%";
                        default -> throw new IllegalStateException("impossible durability style?!");
                    };
                    int width = textRenderer.width(s);
                    int textX, textY;
                    if (vertical) {
                        // textX = x + SIZE + sideOffsetMultiplier * (width + SIZE + 3)
                        // textX = x + (width + 3) * sideOffsetMultiplier + SIZE * (sideOffsetMultiplier + 1)
                        // why am i even trying to optimize this? this is already very clean
                        textX = widgetX + (right ? -width - 2 : SIZE + 2); // if true then it's right, if false it's left
                        textY = y + 4; // center the text vertically
                    } else {
                        float factor = 16F / width; // to adjust from unscaled to scaled you have to divide by the scale factor
                        // center text and cap factor at 1
                        if (factor > 1F) {
                            factor = 1F;
                            textX = x + (16 - width) / 2;
                        } else
                            textX = (int)(x / factor) + 1;
                        // margin of 2 pixels between text and item
                        textY = (int)(anchorTop ?
                            (widgetY + SIZE + 2) / factor :
                            ((widgetY - 2) / factor) - textRenderer.lineHeight + 2); // move down if top, up if bottom
                        matrices.pushMatrix();
                        matrices.scale(factor, factor); // scale
                    }
                    // this math hurt my brain but it works :D
                    context.drawString(textRenderer, s, textX, textY, ARGB.opaque(stack.getBarColor()));
                    if (!vertical)
                        matrices.popMatrix(); // pop 🫧
                }
            } else if (drawBackground) { // background slot icons (if slot is empty and the config says so)
                Identifier spriteId =
                    InventoryMenuAccessor.getTEXTURE_EMPTY_SLOTS().get(SLOT_IDS[index]);
                context.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, x, y, 16, 16);
            }

            if (!stack.isEmpty() || showEmpty)
                if (vertical) { // increase y instead of x if vertical
                    y += STEP;
                } else x += STEP;
        }
    }

    @ExpectPlatform
    public static Path configDir() {
        throw new AssertionError();
    }
}
