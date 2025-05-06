package com.buuz135.functionalstorage.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class PusherUpgradeItem extends UpgradeItem {

    public PusherUpgradeItem() {
        super(new Properties(), Type.UTILITY);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND && player.isCrouching()) {
            ItemStack handStack = player.getItemInHand(hand);
            int slot = handStack.getOrCreateTag().getInt("Slot");
            handStack.getOrCreateTag().putInt("Slot", (slot + 1) % 5);
            player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1);
            String slotString = handStack.getOrCreateTag().getInt("Slot") == 4 ? "All" : handStack.getOrCreateTag().getInt("Slot") + "";
            player.displayClientMessage(new TranslationTextComponent("item.utility.slot").withStyle(TextFormatting.YELLOW).append(new StringTextComponent(slotString).withStyle(TextFormatting.WHITE)), true);
            return ActionResult.success(handStack);
        }
        return super.use(world, player, hand);
    }
}
