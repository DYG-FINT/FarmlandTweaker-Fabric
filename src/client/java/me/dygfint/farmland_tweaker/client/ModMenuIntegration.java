package me.dygfint.farmland_tweaker.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.dygfint.farmland_tweaker.config.ModConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent ->
                //? if >= 1.21.11 {
                /*me.shedaniel.autoconfig.AutoConfigClient
                *///?} else {
                me.shedaniel.autoconfig.AutoConfig //?}
                    .getConfigScreen(ModConfig.class, parent).get();
    }
}