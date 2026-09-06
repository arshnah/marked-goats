package kiwi.allantaylor.markedgoats;
//? if jade_plugin && >=1.21.2 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.component.UseCooldown;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import java.util.Optional;

import static kiwi.allantaylor.markedgoats.util.GoatVariantUtil.getInstrumentNameFromGoat;

@WailaPlugin
public class MarkedGoatsJadePlugin implements IWailaPlugin, IEntityComponentProvider {

    public static final ResourceLocation UID = ResourceLocation.parse("marked_goats:jade_plugin");
    public static final ResourceLocation SHOW_ICON = ResourceLocation.parse("marked_goats:show_icon");
    public static final ResourceLocation SHOW_INSTRUMENT = ResourceLocation.parse("marked_goats:show_instrument");
    public static final ResourceLocation SHOW_IS_SCREAMING = ResourceLocation.parse("marked_goats:show_is_screaming");

    // Remove the cooldown overlay from icons
    public static final UseCooldown FAKE_COOLDOWN = new UseCooldown(0, Optional.of(ResourceLocation.parse("marked_goats:fake_horn_cooldown")));

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(SHOW_ICON, true);
        registration.addConfig(SHOW_INSTRUMENT, true);
        registration.addConfig(SHOW_IS_SCREAMING, true);
        registration.registerEntityComponent(this, Goat.class);
        registration.registerEntityIcon(this, Goat.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Goat goat)) {
            return;
        }
        if (goat.isScreamingGoat() && config.get(SHOW_IS_SCREAMING)) {
            var customName = goat.getCustomName();
            Component fullName = Component.translatable("marked_goats.screaming_goat");

            if (customName != null) {
                fullName = customName.copy().append(" (").append(fullName).append(")");
            }

            tooltip.replace(JadeIds.CORE_OBJECT_NAME, fullName);
        }
        if (config.get(SHOW_INSTRUMENT)) {
            String instrumentName = getInstrumentNameFromGoat(goat);
            tooltip.add(Component.translatable("instrument.minecraft." + instrumentName + "_goat_horn"));
        }
    }

    @Override
    public Element getIcon(EntityAccessor accessor, IPluginConfig config, Element currentIcon) {
        if (!(accessor.getEntity() instanceof Goat goat) || !config.get(SHOW_ICON)) {
            return null;
        }
        var stack = goat.createHorn();
        stack.set(DataComponents.USE_COOLDOWN, FAKE_COOLDOWN);
        return JadeUI.item(stack);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
//?}
