package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LinkingToolItem extends BasicItem {

    public static final String NBT_MODE = "Mode";
    public static final String NBT_CONTROLLER = "Controller";
    public static final String NBT_ACTION = "Action";
    public static final String NBT_FIRST = "First";
    public static final String NBT_ENDER = "Ender";
    public static final String NBT_ENDER_SAFETY = "EnderSafety";

    public static LinkingMode getLinkingMode(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_MODE)) {
            return LinkingMode.valueOf(stack.getOrCreateTag().getString(NBT_MODE));
        }
        return LinkingMode.SINGLE;
    }

    public static ActionMode getActionMode(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_ACTION)) {
            return ActionMode.valueOf(stack.getOrCreateTag().getString(NBT_ACTION));
        }
        return ActionMode.ADD;
    }

    static {
        EventManager.forge(PlayerInteractEvent.LeftClickBlock.class).filter(leftClickBlock -> leftClickBlock.getSide() == LogicalSide.SERVER && leftClickBlock.getItemStack().getItem() instanceof LinkingToolItem).process(leftClickBlock -> {
            ItemStack stack = leftClickBlock.getItemStack();
            TileEntity blockEntity = leftClickBlock.getWorld().getBlockEntity(leftClickBlock.getPos());
            if (blockEntity instanceof EnderDrawerTile) {
                stack.getOrCreateTag().putString(NBT_ENDER, ((EnderDrawerTile) blockEntity).getFrequency());
                leftClickBlock.getPlayer().displayClientMessage(new StringTextComponent("Stored frequency in the tool").setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.color)), true);
                leftClickBlock.setCanceled(true);
            }
        }).subscribe();
    }

    public LinkingToolItem() {
        super(new Properties().tab(FunctionalStorage.TAB).stacksTo(1));
    }

    @Override
    public void onCraftedBy(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftedBy(stack, world, player);
        initNbt(stack);
    }

    private ItemStack initNbt(ItemStack stack) {
        stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.SINGLE.name());
        stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.ADD.name());
        return stack;
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(initNbt(new ItemStack(this)));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrCreateTag().contains(NBT_ENDER);
    }

    @Override
    public boolean canAttackBlock(BlockState state, World level, BlockPos pos, PlayerEntity player) {
        ItemStack stack = player.getItemInHand(Hand.MAIN_HAND);
        TileEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile) {
            stack.getOrCreateTag().putString(NBT_ENDER, ((EnderDrawerTile) blockEntity).getFrequency());
            player.displayClientMessage(new StringTextComponent("Stored frequency in the tool").setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.color)), true);
            return false;
        }
        return super.canAttackBlock(state, level, pos, player);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        World level = context.getLevel();
        TileEntity blockEntity = level.getBlockEntity(pos);
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);
        if (blockEntity instanceof EnderDrawerTile) {
            if (stack.getOrCreateTag().contains(NBT_ENDER)) {
                String frequency = stack.getOrCreateTag().getString(NBT_ENDER);
                EnderInventoryHandler inventory = EnderSavedData.getInstance(context.getLevel()).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                if (inventory.getStackInSlot(0).isEmpty() || (context.getPlayer().isShiftKeyDown() && stack.getOrCreateTag().contains(NBT_ENDER))) {
                    ((EnderDrawerTile) blockEntity).setFrequency(frequency);
                    context.getPlayer().displayClientMessage(new StringTextComponent("Changed drawer frequency").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                    stack.getOrCreateTag().remove(NBT_ENDER_SAFETY);
                } else {
                    context.getPlayer().displayClientMessage(new StringTextComponent("Cannot change frequency, there are items in the drawer. Sneak + Right Click again to ignore this safety").withStyle(TextFormatting.RED), true);
                    stack.getOrCreateTag().putBoolean(NBT_ENDER_SAFETY, true);
                }
                return ActionResultType.SUCCESS;
            }
        }
        if (blockEntity instanceof DrawerControllerTile) {
            CompoundNBT controller = new CompoundNBT();
            controller.putInt("X", pos.getX());
            controller.putInt("Y", pos.getY());
            controller.putInt("Z", pos.getZ());
            stack.getOrCreateTag().put(NBT_CONTROLLER, controller);
            context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 0.5f, 1);
            context.getPlayer().displayClientMessage(new StringTextComponent("Controller configured to the tool").withStyle(TextFormatting.GREEN), true);
            stack.getOrCreateTag().remove(NBT_ENDER);
            return ActionResultType.SUCCESS;
        } else if (blockEntity instanceof ControllableDrawerTile && stack.getOrCreateTag().contains(NBT_CONTROLLER)) {
            CompoundNBT controllerNBT = stack.getOrCreateTag().getCompound(NBT_CONTROLLER);
            TileEntity controller = level.getBlockEntity(new BlockPos(controllerNBT.getInt("X"), controllerNBT.getInt("Y"), controllerNBT.getInt("Z")));
            if (controller instanceof DrawerControllerTile) {
                if (linkingMode == LinkingMode.SINGLE) {
                    ((DrawerControllerTile) controller).addConnectedDrawers(linkingAction, pos);
                    if (linkingAction == ActionMode.ADD) {
                        context.getPlayer().displayClientMessage(new StringTextComponent("Linked drawer to the controller").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                    } else {
                        context.getPlayer().displayClientMessage(new StringTextComponent("Removed drawer from the controller").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                    }
                } else {
                    if (stack.getOrCreateTag().contains(NBT_FIRST)) {
                        CompoundNBT firstpos = stack.getOrCreateTag().getCompound(NBT_FIRST);
                        BlockPos firstPos = new BlockPos(firstpos.getInt("X"), firstpos.getInt("Y"), firstpos.getInt("Z"));
                        AxisAlignedBB aabb = new AxisAlignedBB(Math.min(firstPos.getX(), pos.getX()), Math.min(firstPos.getY(), pos.getY()), Math.min(firstPos.getZ(), pos.getZ()), Math.max(firstPos.getX(), pos.getX()) + 1, Math.max(firstPos.getY(), pos.getY()) + 1, Math.max(firstPos.getZ(), pos.getZ()) + 1);
                        ((DrawerControllerTile) controller).addConnectedDrawers(linkingAction, getBlockPosInAABB(aabb).toArray(new BlockPos[0]));
                        stack.getOrCreateTag().remove(NBT_FIRST);
                        if (linkingAction == ActionMode.ADD) {
                            context.getPlayer().displayClientMessage(new StringTextComponent("Linked drawers to the controller").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                        } else {
                            context.getPlayer().displayClientMessage(new StringTextComponent("Removed drawers from the controller").setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                        }
                    } else {
                        CompoundNBT firstPos = new CompoundNBT();
                        firstPos.putInt("X", pos.getX());
                        firstPos.putInt("Y", pos.getY());
                        firstPos.putInt("Z", pos.getZ());
                        stack.getOrCreateTag().put(NBT_FIRST, firstPos);
                    }
                }
                context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 0.5f, 1);
                return ActionResultType.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (stack.getOrCreateTag().contains(NBT_ENDER)) {
                if (player.isShiftKeyDown()) {
                    stack.getOrCreateTag().remove(NBT_ENDER);
                    player.displayClientMessage(new StringTextComponent("Cleared drawer frequency").setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                }
            } else {
                if (player.isShiftKeyDown()) {
                    LinkingMode linkingMode = getLinkingMode(stack);
                    if (linkingMode == LinkingMode.SINGLE) {
                        stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.MULTIPLE.name());
                        player.displayClientMessage(new StringTextComponent("Swapped mode to " + LinkingMode.MULTIPLE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(LinkingMode.MULTIPLE.getColor())), true);
                    } else {
                        stack.getOrCreateTag().putString(NBT_MODE, LinkingMode.SINGLE.name());
                        player.displayClientMessage(new StringTextComponent("Swapped mode to " + LinkingMode.SINGLE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.getColor())), true);
                    }
                    stack.getOrCreateTag().remove(NBT_FIRST);
                } else {
                    ActionMode linkingMode = getActionMode(stack);
                    if (linkingMode == ActionMode.ADD) {
                        stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.REMOVE.name());
                        player.displayClientMessage(new StringTextComponent("Swapped action to " + ActionMode.REMOVE.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(ActionMode.REMOVE.getColor())), true);
                    } else {
                        stack.getOrCreateTag().putString(NBT_ACTION, ActionMode.ADD.name());
                        player.displayClientMessage(new StringTextComponent("Swapped action to " + ActionMode.ADD.name().toLowerCase(Locale.ROOT)).setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                    }
                }
            }
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
            return ActionResult.success(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<ITextComponent> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);
        if (key == null) {
            if (stack.getOrCreateTag().contains(NBT_ENDER)) {
                TranslationTextComponent text = new TranslationTextComponent("linkingtool.ender.frequency");
                //frequencyDisplay.forEach(item -> text.append(item.getName(new ItemStack(item))));
                tooltip.add(text.withStyle(TextFormatting.GRAY));
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("linkingtool.ender.clear").withStyle(TextFormatting.GRAY));
            } else {
                tooltip.add(new TranslationTextComponent("linkingtool.linkingmode").withStyle(TextFormatting.YELLOW)
                        .append(new TranslationTextComponent("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
                tooltip.add(new TranslationTextComponent("linkingtool.linkingaction").withStyle(TextFormatting.YELLOW)
                        .append(new TranslationTextComponent("linkingtool.linkingaction." + linkingAction.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(linkingAction.getColor()))));
                if (stack.getOrCreateTag().contains(NBT_CONTROLLER)) {
                    tooltip.add(new TranslationTextComponent("linkingtool.controller").withStyle(TextFormatting.YELLOW)
                            .append(new StringTextComponent(stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("X") + "" + TextFormatting.WHITE + ", " + TextFormatting.DARK_AQUA + stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("Y") + TextFormatting.WHITE + ", " + TextFormatting.DARK_AQUA + stack.getOrCreateTag().getCompound(NBT_CONTROLLER).getInt("Z")).withStyle(TextFormatting.DARK_AQUA)));
                } else {
                    tooltip.add(new TranslationTextComponent("linkingtool.controller").withStyle(TextFormatting.YELLOW).append(new StringTextComponent("???").withStyle(TextFormatting.DARK_AQUA)));
                }
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT) + ".desc").withStyle(TextFormatting.GRAY));
                tooltip.add(new TranslationTextComponent("linkingtool.use").withStyle(TextFormatting.GRAY));
            }
        }
    }

    public static List<BlockPos> getBlockPosInAABB(AxisAlignedBB axisAlignedBB) {
        List<BlockPos> blocks = new ArrayList<>();
        for (double y = axisAlignedBB.minY; y < axisAlignedBB.maxY; ++y) {
            for (double x = axisAlignedBB.minX; x < axisAlignedBB.maxX; ++x) {
                for (double z = axisAlignedBB.minZ; z < axisAlignedBB.maxZ; ++z) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
        return blocks;
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }

    public enum LinkingMode {
        SINGLE(net.minecraft.util.text.Color.fromRgb(Color.cyan.getRGB())),
        MULTIPLE(net.minecraft.util.text.Color.fromRgb(Color.GREEN.getRGB()));

        private final net.minecraft.util.text.Color color;

        LinkingMode(net.minecraft.util.text.Color color) {
            this.color = color;
        }

        public net.minecraft.util.text.Color getColor() {
            return color;
        }
    }

    public enum ActionMode {
        ADD(net.minecraft.util.text.Color.fromRgb(new Color(40, 131, 250).getRGB())),
        REMOVE(net.minecraft.util.text.Color.fromRgb(new Color(250, 145, 40).getRGB()));

        private final net.minecraft.util.text.Color color;

        ActionMode(net.minecraft.util.text.Color color) {
            this.color = color;
        }

        public net.minecraft.util.text.Color getColor() {
            return color;
        }
    }
}
