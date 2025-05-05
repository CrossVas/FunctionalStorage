package com.buuz135.functionalstorage.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FunctionalFluidUtils {

    /**
     * Attempts to drain fluid from a single unit of a stackable fluid container into the specified tank
     * of the given {@link IFluidHandler}.
     * <p>
     * This method is designed to safely handle stackable fluid containers by working with only one item
     * from the stack. If the transfer is successful, the original stack is shrunk by one (unless the player
     * is in creative mode), and the resulting empty container is returned to the player's inventory or dropped.
     * </p>
     *
     * @param source   The {@link ItemStack} representing the fluid container(s) to drain. This stack must contain at least one item.
     * @param slot     The index of the target tank in the {@link IFluidHandler}.
     * @param player   The {@link PlayerEntity} performing the interaction. Used to manage inventory and fallback item drops.
     * @param handler  The {@link IFluidHandler} representing the destination tank for the fluid.
     * @return {@code true} if fluid was successfully drained from one container and inserted into the handler;
     *         {@code false} otherwise (e.g., tank full, incompatible fluid, or empty source).
     */
    public static boolean drainContainers(ItemStack source, int slot, PlayerEntity player, IFluidHandler handler) {
        if (source.isEmpty() || handler.getTankCapacity(slot) <= 0) return false;

        // We only work with a single item
        ItemStack single = source.copy();
        single.setCount(1);

        FluidActionResult result = FluidUtil.tryEmptyContainer(single, handler, handler.getTankCapacity(slot), player, true);
        if (!result.isSuccess()) return false;

        // We only shrink if not in creative
        if (!player.isCreative()) {
            source.shrink(1);
            ItemStack resultStack = result.getResult();
            if (!resultStack.isEmpty() && !player.addItem(resultStack)) {
                player.drop(resultStack, false);
            }
        }

        return true;
    }
}
