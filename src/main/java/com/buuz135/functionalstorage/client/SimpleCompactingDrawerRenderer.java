package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.ZERO;
import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class SimpleCompactingDrawerRenderer extends TileEntityRenderer<SimpleCompactingDrawerTile> {

    public SimpleCompactingDrawerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(SimpleCompactingDrawerTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(), FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
            return;
        }
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection();
        matrixStack.last().pose().multiply(createTransformMatrix(
                ZERO, new Vector3f(0, 180, 0), 1));

        if (facing == Direction.NORTH) {
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(-1, 0, 0), ZERO, 1));
        } else if (facing == Direction.EAST) {
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
        } else if (facing == Direction.SOUTH) {
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        } else if (facing == Direction.WEST) {
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(0, 0, 0), new Vector3f(0, 90, 0), 1));
        }

        matrixStack.translate(0, 0, -0.5 / 16D);
        combinedLightIn = WorldRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        ItemStack stack = tile.getHandler().getResultList().get(0).getResult();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(0.5f, 0.27f, 0.0005f), ZERO, new Vector3f(.5f, .5f, 1.0f)));
            DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, tile.getHandler().getStackInSlot(0).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        stack = tile.getHandler().getResultList().get(1).getResult();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(0.5f, 0.77f, 0.0005f), ZERO, new Vector3f(.5f, .5f, 1.0f)));
            DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, tile.getHandler().getStackInSlot(1).getCount(), 0.02f, tile.getDrawerOptions());
            matrixStack.popPose();
        }
        matrixStack.popPose();
    }

}
