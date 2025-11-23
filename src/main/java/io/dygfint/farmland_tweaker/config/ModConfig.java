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
    public IrrigationTweaker irrigationTweaker = new IrrigationTweaker();
    @ConfigEntry.Category("trampleTweaker")
    @ConfigEntry.Gui.TransitiveObject
    public TrampleTweaker trampleTweaker = new TrampleTweaker();
    @ConfigEntry.Category("moistureTweaker")
    @ConfigEntry.Gui.TransitiveObject
    public MoistureTweaker moistureTweaker = new MoistureTweaker();
    @ConfigEntry.Category("livingTweaker")
    @ConfigEntry.Gui.TransitiveObject
    public LivingTweaker livingTweaker = new LivingTweaker();

    public static class IrrigationTweaker {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableIrrigationTweaker = true;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeXZ = 4;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeYmin = 0;
        @ConfigEntry.Gui.RequiresRestart
        public int rangeYmax = 1;
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
        @ConfigEntry.Gui.RequiresRestart
        public boolean allowTramplingFarmlandUnderCrops = true;
        @ConfigEntry.Gui.CollapsibleObject
        public FarmlandTrampleSpread farmlandTrampleSpread = new FarmlandTrampleSpread();

        public static class FarmlandTrampleSpread {
            @ConfigEntry.Gui.RequiresRestart
            public boolean enableSpread = false;
            @ConfigEntry.Gui.RequiresRestart
            public double minSpreadFallDistance = 6.0;
            @ConfigEntry.Gui.Tooltip()
            @ConfigEntry.Gui.RequiresRestart
            public double spreadFallRange = 16.0;
            @ConfigEntry.Gui.RequiresRestart
            public int maxSpreadRadius = 2;
            @ConfigEntry.Gui.RequiresRestart
            public int spreadRangeMinY = -1;
            @ConfigEntry.Gui.RequiresRestart
            public int spreadRangeMaxY = 1;
            @ConfigEntry.Gui.RequiresRestart
            public int glideIncreaseSpreadRadius = 2;
        }
    }

    public static class MoistureTweaker {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableMoistureTweaker = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean preventCropFarmlandDryToDirt = true;
    }

    public static class LivingTweaker {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableLivingTweaker = true;
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.RequiresRestart
        public boolean allowGlidingCollisionTrample = false;
    }
}
