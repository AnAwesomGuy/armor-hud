package ru.berdinskiybear.armorhud.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.Anchor;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.OffhandSlotBehavior;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.Style;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.WidgetShown;

import static net.minecraft.text.Text.translatable;

public interface ArmorHudClothConfig {
    static Screen createScreen(Screen parent) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setTitle(translatable("armorhud.config"))
                                             .setParentScreen(parent)
                                             .setSavingRunnable(config::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        builder.getOrCreateCategory(translatable("armorhud.name"))
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.enabled"), config.enabled)
                          .setSaveConsumer(config::setEnabled).build())
               .addEntry(
                   entries.startEnumSelector(translatable("armorhud.option.anchor"), Anchor.class,
                                             config.anchor)
                          .setEnumNameProvider(e -> Text.translatable(((Anchor)e).translationKey))
                          .setSaveConsumer(config::setAnchor).build())
               .addEntry(
                   entries.startEnumSelector(translatable("armorhud.option.side"), Arm.class, config.side)
                          .setEnumNameProvider(e -> Text.translatable(((Arm)e).getTranslationKey()))
                          .setSaveConsumer(config::setSide).build())
               .addEntry(
                   entries.startIntField(translatable("armorhud.option.offsetX"), config.offsetX)
                          .setSaveConsumer(config::setOffsetX).build())
               .addEntry(
                   entries.startIntField(translatable("armorhud.option.offsetY"), config.offsetY)
                          .setSaveConsumer(config::setOffsetY).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.vertical"), config.vertical)
                          .setSaveConsumer(config::setVertical).build())
               .addEntry(
                   entries.startEnumSelector(translatable("armorhud.option.style"), Style.class,
                                             config.style)
                          .setEnumNameProvider(e -> Text.translatable(((Style)e).translationKey))
                          .setSaveConsumer(config::setStyle).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.durabilityNumbers"),
                                              config.durabilityNumbers)
                          .setSaveConsumer(config::setDurabilityNumbers).build())
               .addEntry(
                   entries.startEnumSelector(translatable("armorhud.option.widgetShown"),
                                             WidgetShown.class,
                                             config.widgetShown)
                          .setEnumNameProvider(e -> Text.translatable(((WidgetShown)e).translationKey))
                          .setSaveConsumer(config::setWidgetShown).build())
               .addEntry(
                   entries.startEnumSelector(translatable("armorhud.option.offhandSlotBehavior"),
                                             OffhandSlotBehavior.class, config.offhandSlotBehavior)
                          .setEnumNameProvider(
                              e -> Text.translatable(((OffhandSlotBehavior)e).translationKey))
                          .setSaveConsumer(config::setOffhandSlotBehavior).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.pushBossbars"), config.pushBossbars)
                          .setSaveConsumer(config::setPushBossbars).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.pushIcons"), config.pushStatusEffectIcons)
                          .setSaveConsumer(config::setPushStatusEffectIcons).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.pushSubtitles"), config.pushSubtitles)
                          .setSaveConsumer(config::setPushSubtitles).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.reversed"), config.reversed)
                          .setSaveConsumer(config::setReversed).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.showIcons"), config.iconsShown)
                          .setSaveConsumer(config::setIconsShown).build())
               .addEntry(
                   entries.startBooleanToggle(translatable("armorhud.option.showWarning"), config.warningShown)
                          .setSaveConsumer(config::setWarningShown).build())
               .addEntry(
                   entries.startIntField(translatable("armorhud.option.minDuraValue"), config.minDurabilityValue)
                          .setSaveConsumer(config::setMinDurabilityValue).build())
               .addEntry(
                   entries.startIntSlider(translatable("armorhud.option.minDuraPercent"),
                                          (int)(config.minDurabilityPercentage * 100.0), 0, 100)
                          .setSaveConsumer(config::setMinDurabilityPercentage).build())
               .addEntry(
                   entries.startIntField(translatable("armorhud.option.iconBobIntensity"), config.warningBobIntensity)
                          .setSaveConsumer(config::setWarningBobIntensity).build());
        return builder.build();
    }
}
