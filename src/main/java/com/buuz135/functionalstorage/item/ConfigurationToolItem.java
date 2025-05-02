package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigurationToolItem extends BasicItem {

    public static final String NBT_MODE = "Mode";

    public static ConfigurationAction getAction(ItemStack stack) {
        if (stack.hasTag()) {
            return ConfigurationAction.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
        }
        return ConfigurationAction.LOCKING;
    }

    public ConfigurationToolItem() {
        super(new Properties().tab(FunctionalStorage.TAB).stacksTo(1));
    }

    @Override
    public void onCraftedBy(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftedBy(stack, world, player);
        initNbt(stack);
    }

    private ItemStack initNbt(ItemStack stack) {
        stack.getOrCreateTag().putString(NBT_MODE, ConfigurationAction.LOCKING.name());
        return stack;
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(initNbt(new ItemStack(this)));
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        World level = context.getLevel();
        TileEntity blockEntity = level.getBlockEntity(pos);
        ConfigurationAction configuractionAction = getAction(stack);
        if (blockEntity instanceof ControllableDrawerTile) {
            if (configuractionAction == ConfigurationAction.LOCKING) {
                ((ControllableDrawerTile<?>) blockEntity).toggleLocking();
            } else {
                ((ControllableDrawerTile<?>) blockEntity).toggleOption(configuractionAction);
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ConfigurationAction action = getAction(stack);
                ConfigurationAction newAction = ConfigurationAction.values()[(Arrays.asList(ConfigurationAction.values()).indexOf(action) + 1) % ConfigurationAction.values().length];
                stack.getOrCreateTag().putString(NBT_MODE, newAction.name());
                player.displayClientMessage(new StringTextComponent("Swapped mode to ").setStyle(Style.EMPTY.withColor(newAction.getColor()))
                        .append(new TranslationTextComponent("configurationtool.configmode." + newAction.name().toLowerCase(Locale.ROOT))), true);
                player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
                return ActionResult.success(stack);
            }
        }
        return super.use(world, player, hand);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<ITextComponent> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        ConfigurationAction linkingMode = getAction(stack);
        if (key == null) {
            tooltip.add(new TranslationTextComponent("configurationtool.configmode").withStyle(TextFormatting.YELLOW)
                    .append(new TranslationTextComponent("configurationtool.configmode." + linkingMode.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
            tooltip.add(new StringTextComponent("").withStyle(TextFormatting.GRAY));
            tooltip.add(new TranslationTextComponent("configurationtool.use").withStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum ConfigurationAction {
        LOCKING(net.minecraft.util.text.Color.fromRgb(new Color(40, 131, 250).getRGB())),
        TOGGLE_NUMBERS(net.minecraft.util.text.Color.fromRgb(new Color(250, 145, 40).getRGB())),
        TOGGLE_RENDER(net.minecraft.util.text.Color.fromRgb(new Color(100, 250, 40).getRGB())),
        TOGGLE_UPGRADES(net.minecraft.util.text.Color.fromRgb(new Color(166, 40, 250).getRGB()));

        private final net.minecraft.util.text.Color color;

        ConfigurationAction(net.minecraft.util.text.Color color) {
            this.color = color;
        }

        public net.minecraft.util.text.Color getColor() {
            return color;
        }
    }
}
