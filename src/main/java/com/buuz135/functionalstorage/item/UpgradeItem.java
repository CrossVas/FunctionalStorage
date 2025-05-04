package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UpgradeItem extends BasicItem {

    public static Direction getDirection(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Direction")) {
            Item item = stack.getItem();
            if (item.equals(FunctionalItems.PULLING_UPGRADE.get()) || item.equals(FunctionalItems.PUSHING_UPGRADE.get()) || item.equals(FunctionalItems.COLLECTOR_UPGRADE.get())) {
                Direction direction = Direction.byName(stack.getOrCreateTag().getString("Direction"));
                return direction == null ? Direction.NORTH : direction;
            }
        }
        return Direction.NORTH;
    }

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties.tab(FunctionalStorage.TAB));
        this.type = type;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            ItemStack handStack = player.getItemInHand(hand);
            Item upgrade = handStack.getItem();
            if (upgrade.equals(FunctionalItems.PULLING_UPGRADE.get()) || upgrade.equals(FunctionalItems.PUSHING_UPGRADE.get()) || upgrade.equals(FunctionalItems.COLLECTOR_UPGRADE.get())) {
                Direction direction = getDirection(handStack);
                Direction next = Direction.values()[(Arrays.asList(Direction.values()).indexOf(direction) + 1 ) % Direction.values().length];
                handStack.getOrCreateTag().putString("Direction", next.getName());
                player.playSound(SoundEvents.UI_BUTTON_CLICK, .5f, 1);
                player.displayClientMessage(new TranslationTextComponent("item.utility.direction").withStyle(TextFormatting.YELLOW).append(new TranslationTextComponent(WordUtils.capitalize(getDirection(handStack).getName().toLowerCase(Locale.ROOT))).withStyle(TextFormatting.WHITE)), true);
                return ActionResult.success(handStack);
            }
            if (upgrade.equals(FunctionalItems.REDSTONE_UPGRADE.get())){
                int slot = handStack.getOrCreateTag().getInt("Slot");
                handStack.getOrCreateTag().putInt("Slot", (slot + 1) % 4);
                player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1);
                player.displayClientMessage(new TranslationTextComponent("item.utility.slot").withStyle(TextFormatting.YELLOW).append(new StringTextComponent(slot + "").withStyle(TextFormatting.WHITE)), true);
                return ActionResult.success(handStack);
            }
        }

        return super.use(world, player, hand);
    }

    @Override
    public void onCraftedBy(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftedBy(stack, world, player);
        initNbt(stack);
    }

    private ItemStack initNbt(ItemStack stack) {
        Item item = stack.getItem();
        if (item.equals(FunctionalItems.PULLING_UPGRADE.get()) || item.equals(FunctionalItems.PUSHING_UPGRADE.get()) || item.equals(FunctionalItems.COLLECTOR_UPGRADE.get())) {
            stack.getOrCreateTag().putString("Direction", Direction.values()[0].getName());
        }
        if (item.equals(FunctionalItems.REDSTONE_UPGRADE.get())) {
            stack.getOrCreateTag().putInt("Slot", 0);
        }
        return stack;
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(initNbt(new ItemStack(this)));
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<ITextComponent> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        tooltip.add(new TranslationTextComponent("upgrade.type").withStyle(TextFormatting.YELLOW).append(new TranslationTextComponent("upgrade.type." + getType().name().toLowerCase(Locale.ROOT)).withStyle(TextFormatting.WHITE)));
        Item item = stack.getItem();
        if (stack.hasTag()) {
            if (item.equals(FunctionalItems.PULLING_UPGRADE.get()) || item.equals(FunctionalItems.PUSHING_UPGRADE.get()) || item.equals(FunctionalItems.COLLECTOR_UPGRADE.get())) {
                tooltip.add(new TranslationTextComponent("item.utility.direction").withStyle(TextFormatting.YELLOW).append(new TranslationTextComponent(WordUtils.capitalize(getDirection(stack).getName().toLowerCase(Locale.ROOT))).withStyle(TextFormatting.WHITE)));
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("item.utility.direction.desc").withStyle(TextFormatting.GRAY));
            }
            if (item.equals(FunctionalItems.REDSTONE_UPGRADE.get())) {
                tooltip.add(new TranslationTextComponent("item.utility.slot").withStyle(TextFormatting.YELLOW).append(new StringTextComponent(stack.getOrCreateTag().getInt("Slot") + "").withStyle(TextFormatting.WHITE)));
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("item.utility.direction.desc").withStyle(TextFormatting.GRAY));
            }
        }

    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum Type {
        STORAGE,
        UTILITY
    }
}
