package io.dygfint.farmland_tweaker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.ArrayList;
import java.util.List;

import static io.dygfint.farmland_tweaker.farmland_tweaker.isInitConfig;

@Config(name = "farmland_tweaker")
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

    @ConfigEntry.Category("irrigationTweaker")
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Gui.RequiresRestart
    public IrrigationTweaker irrigationTweaker = new IrrigationTweaker();
    @ConfigEntry.Category("trampleTweaker")
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Gui.RequiresRestart
    public TrampleTweaker trampleTweaker = new TrampleTweaker();

    public static class IrrigationTweaker {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableIrrigationTweaker = true;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeXZ = 4;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeYmin = -1;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeYmax = 0;
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.Gui.RequiresRestart
        public List<String> extraHydrationBlocks = new ArrayList<>();
    }

    public static class TrampleTweaker {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableTrampleTweaker = true;
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.Gui.RequiresRestart
        public double minTrampleFallHeight = 0.5;
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.Gui.RequiresRestart
        public double trampleFallRange = 1.0;
        @ConfigEntry.Gui.RequiresRestart
        public boolean requireLivingEntityToTrample = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean allowPlayerTrample = true;
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.RequiresRestart
        public boolean allowMobTrample = true;
        @ConfigEntry.Gui.RequiresRestart
        public double trampleVolumeThreshold = 0.512;
    }
}
