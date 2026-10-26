package net.xxlenderchest.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.config.XXLConfig;

/**
 * Builds the Cloth Config screen for XXL Enderchest.
 */
public final class XXLEnderChestClothConfigScreen {

    private XXLEnderChestClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        XXLConfig editedConfig = XXLEnderChest.loadConfigForEditing();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("XXL Enderchest Config"));

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Enable XXL Enderchest"),
                        editedConfig.isEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("When disabled, the ender chest stays at the vanilla 3 rows."))
                .setSaveConsumer(editedConfig::setEnabled)
                .build());

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Use LuckPerms row permissions"),
                        editedConfig.isUseLuckPerms())
                .setDefaultValue(false)
                .setTooltip(Component.literal("If enabled, permissions decide whether players get 4, 5, or 6 rows."))
                .setSaveConsumer(editedConfig::setUseLuckPerms)
                .build());

        general.addEntry(entries.startIntSlider(
                        Component.literal("Fallback row count"),
                        editedConfig.getRows(),
                        3,
                        6)
                .setDefaultValue(6)
                .setTooltip(Component.literal("Used when LuckPerms mode is disabled, or when LuckPerms is not installed."))
                .setSaveConsumer(editedConfig::setRows)
                .build());

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Enable /enderchest command"),
                        editedConfig.isCommandEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("When enabled, players can use /enderchest. In LuckPerms mode they also need the xxlenderchest.command.enderchest node."))
                .setSaveConsumer(editedConfig::setCommandEnabled)
                .build());

        builder.setSavingRunnable(() -> XXLEnderChest.applyEditedConfig(editedConfig));
        return builder.build();
    }
}
