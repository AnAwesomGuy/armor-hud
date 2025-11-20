package ru.berdinskiybear.armorhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.Arm;
import ru.berdinskiybear.armorhud.ArmorHudMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ArmorHudConfig implements Serializable {
    public static final ArmorHudConfig CONFIG = new ArmorHudConfig();
    public static final Path FILE = ArmorHudMod.configDir().resolve(ArmorHudMod.MOD_ID + ".json");
    public static final Gson GSON =
        new GsonBuilder().setLenient()
                         .setPrettyPrinting()
                         .registerTypeAdapter(ArmorHudConfig.class, (InstanceCreator<ArmorHudConfig>)type -> CONFIG)
                         .create();

    static {
        CONFIG.load(); // if i run this in the constructor, the other fields are still null
    }

    private ArmorHudConfig() {
    }

    public boolean enabled = true;
    public Anchor anchor = Anchor.HOTBAR;
    public Arm side = Arm.LEFT;
    public int offsetX = 0;
    public int offsetY = 0;
    public boolean vertical = false;
    public Style style = Style.HOTBAR;
    public boolean durabilityNumbers = false;
    public WidgetShown widgetShown = WidgetShown.NOT_EMPTY;
    public OffhandSlotBehavior offhandSlotBehavior = OffhandSlotBehavior.ADHERE;
    public boolean pushBossbars = true;
    public boolean pushStatusEffectIcons = true;
    public boolean pushSubtitles = true;
    public boolean reversed = false;
    public boolean iconsShown = true;
    public boolean warningShown = true;
    public int minDurabilityValue = 5;
    public double minDurabilityPercentage = 0.05;
    public int warningBobIntensity = 3;

    //region // getter and setters
    //@formatter:off
    public boolean isDisabled() {
        return !enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public Anchor getAnchor() {
        return anchor;
    }
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }
    public Arm getSide() {
        return side;
    }
    public void setSide(Arm side) {
        this.side = side;
    }
    public int getOffsetX() {
        return offsetX;
    }
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    public int getOffsetY() {
        return offsetY;
    }
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
    public boolean isVertical() {
        return vertical;
    }
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
    public Style getStyle() {
        return style;
    }
    public void setStyle(Style style) {
        this.style = style;
    }
    public boolean isDurabilityNumbers() {
        return durabilityNumbers;
    }
    public void setDurabilityNumbers(boolean durabilityNumbers) {
        this.durabilityNumbers = durabilityNumbers;
    }
    public WidgetShown getWidgetShown() {
        return widgetShown;
    }
    public void setWidgetShown(WidgetShown widgetShown) {
        this.widgetShown = widgetShown;
    }
    public OffhandSlotBehavior getOffhandSlotBehavior() {
        return offhandSlotBehavior;
    }
    public void setOffhandSlotBehavior(OffhandSlotBehavior offhandSlotBehavior) {
        this.offhandSlotBehavior = offhandSlotBehavior;
    }
    public boolean isPushBossbars() {
        return pushBossbars;
    }
    public void setPushBossbars(boolean pushBossbars) {
        this.pushBossbars = pushBossbars;
    }
    public boolean isPushStatusEffectIcons() {
        return pushStatusEffectIcons;
    }
    public void setPushStatusEffectIcons(boolean pushStatusEffectIcons) {
        this.pushStatusEffectIcons = pushStatusEffectIcons;
    }
    public boolean isPushSubtitles() {
        return pushSubtitles;
    }
    public void setPushSubtitles(boolean pushSubtitles) {
        this.pushSubtitles = pushSubtitles;
    }
    public boolean isReversed() {
        return reversed;
    }
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
    public boolean isIconsShown() {
        return iconsShown;
    }
    public void setIconsShown(boolean iconsShown) {
        this.iconsShown = iconsShown;
    }
    public boolean isWarningShown() {
        return warningShown;
    }
    public void setWarningShown(boolean warningShown) {
        this.warningShown = warningShown;
    }
    public int getMinDurabilityValue() {
        return minDurabilityValue;
    }
    public void setMinDurabilityValue(int minDurabilityValue) {
        this.minDurabilityValue = minDurabilityValue;
    }
    public double getMinDurabilityPercentage() {
        return minDurabilityPercentage;
    }
    public void setMinDurabilityPercentage(double minDurabilityPercentage) {
        this.minDurabilityPercentage = minDurabilityPercentage;
    }
    public void setMinDurabilityPercentage(int minDurabilityPercentage) {
        this.minDurabilityPercentage = minDurabilityPercentage / 100.0;
    }
    public int getWarningBobIntensity() {
        return warningBobIntensity;
    }
    public void setWarningBobIntensity(int warningBobIntensity) {
        this.warningBobIntensity = warningBobIntensity;
    }
    //@formatter:on
    //endregion

    public void toggleEnabled() {
        setEnabled(isDisabled());
    }

    public enum Anchor {
        TOP_CENTER("armorhud.option.topCenter"),
        TOP("armorhud.option.top"),
        BOTTOM("armorhud.option.bottom"),
        HOTBAR("armorhud.option.hotbar");

        public final String translationKey;

        Anchor(String translationKey) {
            this.translationKey = translationKey;
        }

        public boolean isTop() {
            return this == TOP || this == TOP_CENTER;
        }
    }

    public enum Style {
        HOTBAR("armorhud.option.hotbar"),
        ROUNDED_CORNERS("armorhud.option.roundedCorners"),
        ROUNDED("armorhud.option.rounded"),
        NONE("armorhud.option.none");

        public final String translationKey;

        Style(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public enum WidgetShown {
        ALWAYS("armorhud.option.always"),
        IF_ANY_PRESENT("armorhud.option.ifAnyPresent"),
        NOT_EMPTY("armorhud.option.notEmpty");

        public final String translationKey;

        WidgetShown(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public enum OffhandSlotBehavior {
        ALWAYS_IGNORE("armorhud.option.alwaysIgnore"),
        ADHERE("armorhud.option.adhere"),
        ALWAYS_LEAVE_SPACE("armorhud.option.alwaysLeaveSpace");

        public final String translationKey;

        OffhandSlotBehavior(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public void load() {
        if (!Files.exists(FILE))
            save();
        else
            try (Reader reader = Files.newBufferedReader(FILE)) {
                GSON.fromJson(reader, ArmorHudConfig.class);
            } catch (JsonSyntaxException | IOException e) {
                ArmorHudMod.LOGGER.error("Error reading config! Reloading from defaults!", e);
                save();
            }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(FILE, StandardOpenOption.CREATE,
                                                     StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException e) {
            ArmorHudMod.LOGGER.error("Unable to write config to file!", e);
        }
    }
}
