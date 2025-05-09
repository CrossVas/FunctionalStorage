package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.util.DrawerType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class FluidDrawerRenderer extends TileEntityRenderer<FluidDrawerTile> {

    public FluidDrawerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(FluidDrawerTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(), FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
            return;
        }
        matrixStack.pushPose();
        Direction facing = tile.getFacingDirection();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));
        if (facing == Direction.NORTH) {
            matrixStack.translate(-1, 0, -1);
        }
        if (facing == Direction.EAST) {
            matrixStack.translate(0, 0, -1);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
        }
        if (facing == Direction.SOUTH) {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-180));
        }
        if (facing == Direction.WEST) {
            matrixStack.translate(-1, 0, 0);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        }
        combinedLightIn = WorldRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));

        if (tile.getDrawerType() == DrawerType.X_1)
            render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == DrawerType.X_2)
            render2Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        if (tile.getDrawerType() == DrawerType.X_4)
            render4Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 0.9688);
        RenderHelper.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.popPose();
        matrixStack.popPose();
    }

    private void render1Slot(MatrixStack ms, IRenderTypeBuffer buf, int light, int overlay, FluidDrawerTile tile) {
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 0, 12.5 / 16D, null, false, 15 / 16D);
    }

    private void render2Slot(MatrixStack ms, IRenderTypeBuffer buf, int light, int overlay, FluidDrawerTile tile) {
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 0, 5.5 / 16D, null, false, 15 / 16D);
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 1, 5.5 / 16D, new Vector3d(0, 0.5, 0), false, 15 / 16D);
    }

    private void render4Slot(MatrixStack ms, IRenderTypeBuffer buf, int light, int overlay, FluidDrawerTile tile) {
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 0, 5.5 / 16D, new Vector3d(0.5, 0, 0), true, 8 / 16D);
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 1, 5.5 / 16D, null, true, 8 / 16D);
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 2, 5.5 / 16D, new Vector3d(0.5, 0.5, 0), true, 8 / 16D);
        RenderHelper.renderFluidSlot(ms, buf, light, overlay, tile, 3, 5.5 / 16D, new Vector3d(0, 0.5, 0), true, 8 / 16D);
    }
}
