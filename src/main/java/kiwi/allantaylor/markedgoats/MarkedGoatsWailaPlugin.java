package kiwi.allantaylor.markedgoats;
//? if wthit_plugin && >=1.21.2 {
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.component.UseCooldown;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

public class MarkedGoatsWailaPlugin implements IWailaCommonPlugin, IWailaClientPlugin, IEntityComponentProvider {

    public static final ResourceLocation SHOW_ICON = ResourceLocation.parse("marked_goats:show_icon");
    public static final ResourceLocation SHOW_INSTRUMENT = ResourceLocation.parse("marked_goats:show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = ResourceLocation.parse("marked_goats:show_is_screaming");

    // Remove the cooldown overlay from icons
    public static final UseCooldown FAKE_COOLDOWN = new UseCooldown(0, Optional.of(ResourceLocation.parse("marked_goats:fake_horn_cooldown")));

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.localConfig(SHOW_ICON, true);
        registrar.localConfig(SHOW_INSTRUMENT, true);
        registrar.localConfig(SHOW_IS_SCREAMING, true);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.head(this, Goat.class);
        registrar.body(this, Goat.class);
        registrar.icon(this, Goat.class);
    }

    @Override
    public void appendHead(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getEntity() instanceof Goat goat && goat.isScreamingGoat() && config.getBoolean(SHOW_IS_SCREAMING)) {
            var formatter = IWailaConfig.get().getFormatter();
            var customName = goat.getCustomName();
            Component fullName = Component.translatable("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(fullName.getString()));
        }
    }

    @Override
    public void appendBody(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return;
        }
        if (config.getBoolean(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(entity);
            tooltip.addLine(Component.translatable("instrument.minecraft." + instrumentName + "_goat_horn"));
        }
    }

    @Override
    public ITooltipComponent getIcon(IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return null;
        }
        if (config.getBoolean(SHOW_ICON)) {
            var stack = entity.createHorn();
            stack.set(DataComponents.USE_COOLDOWN, FAKE_COOLDOWN);
            return new ItemComponent(stack);
        }
        return null;
    }
}
//?}
//? if wthit_plugin && >=1.21 && <1.21.2 {
/*import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.NotNull;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

public class MarkedGoatsWailaPlugin implements IWailaCommonPlugin, IWailaClientPlugin, IEntityComponentProvider {

    public static final ResourceLocation SHOW_ICON = ResourceLocation.parse("marked_goats:show_icon");
    public static final ResourceLocation SHOW_INSTRUMENT = ResourceLocation.parse("marked_goats:show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = ResourceLocation.parse("marked_goats:show_is_screaming");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.localConfig(SHOW_ICON, true);
        registrar.localConfig(SHOW_INSTRUMENT, true);
        registrar.localConfig(SHOW_IS_SCREAMING, true);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.head(this, Goat.class);
        registrar.body(this, Goat.class);
        registrar.icon(this, Goat.class);
    }

    @Override
    public void appendHead(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getEntity() instanceof Goat goat && goat.isScreamingGoat() && config.getBoolean(SHOW_IS_SCREAMING)) {
            var formatter = IWailaConfig.get().getFormatter();
            var customName = goat.getCustomName();
            Component fullName = Component.translatable("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(fullName.getString()));
        }
    }

    @Override
    public void appendBody(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return;
        }
        if (config.getBoolean(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(entity);
            tooltip.addLine(Component.translatable("instrument.minecraft." + instrumentName + "_goat_horn"));
        }
    }

    @Override
    public ITooltipComponent getIcon(IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return null;
        }
        if (config.getBoolean(SHOW_ICON)) {
            // No use_cooldown component exists yet at this version to fake out the
            // cooldown overlay (added in 1.21.2's item-component overhaul) - a fresh
            // stack from createHorn() has no real cooldown applied anyway.
            return new ItemComponent(entity.createHorn());
        }
        return null;
    }
}
*///?}
//? if wthit_plugin && <1.21 && !wthit_plugin_legacy {
/*import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.NotNull;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

public class MarkedGoatsWailaPlugin implements IWailaCommonPlugin, IWailaClientPlugin, IEntityComponentProvider {

    public static final ResourceLocation SHOW_ICON = new ResourceLocation("marked_goats", "show_icon");
    public static final ResourceLocation SHOW_INSTRUMENT = new ResourceLocation("marked_goats", "show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = new ResourceLocation("marked_goats", "show_is_screaming");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.localConfig(SHOW_ICON, true);
        registrar.localConfig(SHOW_INSTRUMENT, true);
        registrar.localConfig(SHOW_IS_SCREAMING, true);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.head(this, Goat.class);
        registrar.body(this, Goat.class);
        registrar.icon(this, Goat.class);
    }

    @Override
    public void appendHead(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getEntity() instanceof Goat goat && goat.isScreamingGoat() && config.getBoolean(SHOW_IS_SCREAMING)) {
            var formatter = IWailaConfig.get().getFormatter();
            var customName = goat.getCustomName();
            Component fullName = Component.translatable("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(fullName.getString()));
        }
    }

    @Override
    public void appendBody(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return;
        }
        if (config.getBoolean(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(entity);
            tooltip.addLine(Component.translatable("instrument.minecraft." + instrumentName + "_goat_horn"));
        }
    }

    @Override
    public ITooltipComponent getIcon(IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return null;
        }
        if (config.getBoolean(SHOW_ICON)) {
            return new ItemComponent(entity.createHorn());
        }
        return null;
    }
}
*///?}
//? if wthit_plugin_legacy && >=1.19 {
/*import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.NotNull;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

// WTHIT's own PLUGIN API here is the old, pre-split generation - a single
// IWailaPlugin.register(IRegistrar) instead of the modern
// IWailaCommonPlugin/IWailaClientPlugin split. IEntityComponentProvider
// itself (getIcon/appendHead/appendBody below) is byte-for-byte identical
// between generations though, confirmed by decompiling both - only the
// registration calls differ (registrar.addComponent/addIcon/addConfig here
// vs the modern registrar.head/icon/localConfig shortcuts).
public class MarkedGoatsWailaPlugin implements IWailaPlugin, IEntityComponentProvider {

    public static final ResourceLocation SHOW_ICON = new ResourceLocation("marked_goats", "show_icon");
    public static final ResourceLocation SHOW_INSTRUMENT = new ResourceLocation("marked_goats", "show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = new ResourceLocation("marked_goats", "show_is_screaming");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(SHOW_ICON, true);
        registrar.addConfig(SHOW_INSTRUMENT, true);
        registrar.addConfig(SHOW_IS_SCREAMING, true);
        registrar.addComponent(this, TooltipPosition.HEAD, Goat.class);
        registrar.addComponent(this, TooltipPosition.BODY, Goat.class);
        registrar.addIcon(this, Goat.class);
    }

    @Override
    public void appendHead(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getEntity() instanceof Goat goat && goat.isScreamingGoat() && config.getBoolean(SHOW_IS_SCREAMING)) {
            var formatter = IWailaConfig.get().getFormatter();
            var customName = goat.getCustomName();
            Component fullName = Component.translatable("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(fullName.getString()));
        }
    }

    @Override
    public void appendBody(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return;
        }
        if (config.getBoolean(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(entity);
            tooltip.addLine(Component.translatable("instrument.minecraft." + instrumentName + "_goat_horn"));
        }
    }

    @Override
    public ITooltipComponent getIcon(IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return null;
        }
        if (config.getBoolean(SHOW_ICON)) {
            return new ItemComponent(entity.createHorn());
        }
        return null;
    }
}
*///?}
//? if wthit_plugin_legacy && <1.19 {
/*import mcp.mobius.waila.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.NotNull;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

// 1.18.2 predates goat horns entirely (added in 1.19's Wild Update):
// Goat#createHorn() doesn't exist at all (confirmed via javap on the real
// Goat.class), so there's no item to build an icon from - no SHOW_ICON
// config, no addIcon call, no getIcon override (IEntityComponentProvider's
// default already returns null, so WTHIT just never asks). Component's
// static translatable()/literal() factories don't exist yet either
// (confirmed - Component only has nullToEmpty(String) here), so text is
// built via the direct TranslatableComponent/TextComponent constructors
// instead. The instrument name is shown as a plain literal, not translated
// - the vanilla instrument.minecraft.X_goat_horn key doesn't exist in
// 1.18.2's lang files (no horn item registered to own it).
public class MarkedGoatsWailaPlugin implements IWailaPlugin, IEntityComponentProvider {

    public static final ResourceLocation SHOW_INSTRUMENT = new ResourceLocation("marked_goats", "show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = new ResourceLocation("marked_goats", "show_is_screaming");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(SHOW_INSTRUMENT, true);
        registrar.addConfig(SHOW_IS_SCREAMING, true);
        registrar.addComponent(this, TooltipPosition.HEAD, Goat.class);
        registrar.addComponent(this, TooltipPosition.BODY, Goat.class);
    }

    @Override
    public void appendHead(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getEntity() instanceof Goat goat && goat.isScreamingGoat() && config.getBoolean(SHOW_IS_SCREAMING)) {
            var formatter = IWailaConfig.get().getFormatter();
            var customName = goat.getCustomName();
            Component fullName = new TranslatableComponent("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(fullName.getString()));
        }
    }

    @Override
    public void appendBody(@NotNull ITooltip tooltip, IEntityAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat entity)) {
            return;
        }
        if (config.getBoolean(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(entity);
            // No vanilla translation exists at this version to capitalize it
            // for us (see the class-level comment above), so do it by hand.
            String capitalized = instrumentName.substring(0, 1).toUpperCase() + instrumentName.substring(1);
            tooltip.addLine(new TextComponent(capitalized));
        }
    }
}
*///?}
