package me.dygfint.farmland_tweaker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.ArrayList;
import java.util.List;

import static me.dygfint.farmland_tweaker.farmland_tweaker.isInitConfig;

@Config(name = "farmland_tweaker")
public class ModConfig implements ConfigData {
    public static void init() {
        if (!isInitConfig) {
            AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
            isInitConfig = true;
        }
    }

    public static ModConfig get() {
        init();
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

    public static class IrrigationTweaker {
        public boolean enableIrrigationTweaker = true;
        public int rangeXZ = 4;
        public int rangeYmin = 0;
        public int rangeYmax = 1;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> extraHydrationBlocks = new ArrayList<>();
    }

    public static class TrampleTweaker {
        public boolean enableTrampleTweaker = true;
        public boolean requireLivingEntityToTrample = true;
        public boolean allowPlayerTrample = true;
        @ConfigEntry.Gui.Tooltip()
        public boolean allowMobTrample = true;
        @ConfigEntry.Gui.CollapsibleObject
        public DefaultTweaker defaultTweaker = new DefaultTweaker();
        @ConfigEntry.Gui.CollapsibleObject
        public GlideTweaker glideTweaker = new GlideTweaker();
        @ConfigEntry.Gui.CollapsibleObject
        public ForceTrampleOnGround forceTrampleOnGround = new ForceTrampleOnGround();
        @ConfigEntry.Gui.CollapsibleObject
        public FarmlandTrampleSpread farmlandTrampleSpread = new FarmlandTrampleSpread();

        public static class DefaultTweaker {
            @ConfigEntry.Gui.Tooltip(count = 2)
            public double minTrampleBPS = 3.10;
            @ConfigEntry.Gui.Tooltip(count = 2)
            public double trampleBPSRange = 7.53;
            public double trampleVolumeThreshold = 0.512;
            public boolean allowTramplingFarmlandUnderCrops = true;
        }

        public static class GlideTweaker {
            //? if >=1.21.2 {
            @ConfigEntry.Gui.Tooltip(count = 2)
            public double minGlideTrampleBPS = 0.2052997014;
            @ConfigEntry.Gui.Tooltip(count = 2)
            public double glideTrampleBPSRange = -1.0;
            //?} else {
            /*@ConfigEntry.Gui.Tooltip(count = 2)
            public double minGlideTrampleBPS = 3.10;
            @ConfigEntry.Gui.Tooltip(count = 2)
            public double glideTrampleBPSRange = 7.53;
            *///?}
            public double glideTrampleVolumeThreshold = 0.216;
            public boolean allowGlideTramplingFarmlandUnderCrops = true;
        }

        public static class ForceTrampleOnGround {
            public boolean enableForceTrampleOnGround = false;
            public double minGroundTrampleVolume = 0.648;
        }

        public static class FarmlandTrampleSpread {
            public boolean enableSpread = false;
            @ConfigEntry.Gui.CollapsibleObject
            public DefaultSpreadRadius defaultSpreadRadius = new DefaultSpreadRadius();
            @ConfigEntry.Gui.CollapsibleObject
            public GlideSpreadRadius glideSpreadRadius = new GlideSpreadRadius();
            @ConfigEntry.Gui.Tooltip()
            @ConfigEntry.Gui.CollapsibleObject
            public VolumeScaling volumeScaling = new VolumeScaling();
            public int spreadRangeMinY = -1;
            public int spreadRangeMaxY = 1;

            public static class DefaultSpreadRadius {
                public int baseSpreadRadius = 2;
                public double minSpreadBPS = 11.70;
                @ConfigEntry.Gui.Tooltip()
                public double spreadBPSRange = 31.09;
                public double volumeCorrectionDivisor = 0.648;
            }

            public static class GlideSpreadRadius {
                public int glideBaseSpreadRadius = 4;
                public double glideMinSpreadBPS = 16.88;
                @ConfigEntry.Gui.Tooltip()
                public double glideSpreadBPSRange = 42.01;
                public double glideVolumeCorrectionDivisor = 0.216;
            }

            public static class VolumeScaling {
                public boolean enableVolumeScaling = true;
                @ConfigEntry.Gui.Tooltip()
                public double volumeClampMax = 10;
                @ConfigEntry.Gui.Tooltip(count = 7)
                @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
                public VolumeScaleMode volumeScaleMode = VolumeScaleMode.linear;
                @ConfigEntry.Gui.Tooltip
                public double volumeScaleMin = 0.5;
                @ConfigEntry.Gui.Tooltip
                public double volumeScaleMax = 1.5;

                public enum VolumeScaleMode {linear, sqrt, cbrt, quadratic, cubic, log}
            }
        }
    }

    public static class MoistureTweaker {
        public boolean enableMoistureTweaker = true;
        public boolean preventCropFarmlandDryToDirt = true;
    }
}
