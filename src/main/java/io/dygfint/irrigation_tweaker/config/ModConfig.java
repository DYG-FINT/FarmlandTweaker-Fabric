package io.dygfint.irrigation_tweaker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.ArrayList;
import java.util.List;

import static io.dygfint.irrigation_tweaker.Irrigation_tweaker.isInitConfig;

@Config(name = "irrigation_tweaker")
public class ModConfig implements ConfigData {
    public static void init() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        isInitConfig = true;
    }

    public static ModConfig get() {
        if (!isInitConfig) {
            init();
        }

        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    @ConfigEntry.Gui.RequiresRestart
    public boolean enable = true;

    @ConfigEntry.Gui.RequiresRestart
    public int rangeXZ = 4;
    @ConfigEntry.Gui.RequiresRestart
    public int rangeYmin = -1;
    @ConfigEntry.Gui.RequiresRestart
    public int rangeYmax = 0;
    @ConfigEntry.Gui.RequiresRestart
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<String> extraHydrationBlocks = new ArrayList<>();
}
