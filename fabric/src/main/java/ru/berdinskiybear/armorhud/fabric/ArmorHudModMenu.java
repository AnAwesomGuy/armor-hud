package ru.berdinskiybear.armorhud.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import ru.berdinskiybear.armorhud.config.ArmorHudClothConfig;

public class ArmorHudModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FabricLoader.getInstance().isModLoaded("cloth-config") ?
            ArmorHudClothConfig::createScreen : ModMenuApi.super.getModConfigScreenFactory();
    }
}
