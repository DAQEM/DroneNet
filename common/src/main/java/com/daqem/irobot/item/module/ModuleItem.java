package com.daqem.irobot.item.module;

import com.daqem.irobot.IRobot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ModuleItem extends Item implements IModuleItem {

    private final ModuleType type;

    public ModuleItem(Properties properties, ModuleType type) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public ModuleType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(IRobot.translatable("tooltip.module." + type.getSerializedName() + ".description").withStyle(ChatFormatting.GRAY));
    }

    public enum ModuleType {
        SPEED_BOOST("speed_boost"),
        MINING_SPEED("mining_speed"),
        ATTACK_DAMAGE("attack_damage"),
        DURABILITY("durability"),
        BATTERY_EFFICIENCY("battery_efficiency"),
        SOLAR_PANEL("solar_panel"),
        REFORESTATION("reforestation"),
        CROP_REPLANT("crop_replant");

        private final String serializedName;

        ModuleType(String serializedName) {
            this.serializedName = serializedName;
        }

        public String getSerializedName() {
            return serializedName;
        }
    }
}