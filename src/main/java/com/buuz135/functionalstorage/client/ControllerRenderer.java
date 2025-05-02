package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.List;
import java.util.OptionalDouble;

import static com.buuz135.functionalstorage.item.LinkingToolItem.NBT_CONTROLLER;
import static com.buuz135.functionalstorage.item.LinkingToolItem.NBT_FIRST;

public class ControllerRenderer extends TileEntityRenderer<DrawerControllerTile> {

    public static RenderType TYPE = RenderType.create("custom_lines", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 4, 256, false, false, RenderType.State.builder()
            .setShadeModelState(new RenderState.ShadeModelState(false))
            .setDepthTestState(new RenderState.DepthTestState("always", 519))
            .setLineState(new RenderState.LineState(OptionalDouble.empty()))
            .setLayeringState(new RenderState.LayerState("view_offset_z_layering", () -> {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.99975586F, 0.99975586F, 0.99975586F);
            }, RenderSystem::popMatrix))
            .setCullState(new RenderState.CullState(false))
            .createCompositeState(false));

    public ControllerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }


    private static void renderShape(MatrixStack matrixStack, IVertexBuilder vertexBuilder, VoxelShape voxelShape, double x, double y, double z, float r, float g, float b, float a) {
        MatrixStack.Entry matrixPose = matrixStack.last();
        voxelShape.forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
            float f = (float) (p_194327_ - p_194324_);
            float f1 = (float) (p_194328_ - p_194325_);
            float f2 = (float) (p_194329_ - p_194326_);
            float f3 = MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            vertexBuilder.vertex(matrixPose.pose(), (float) (p_194324_ + x), (float) (p_194325_ + y), (float) (p_194326_ + z)).color(r, g, b, a).normal(matrixPose.normal(), f, f1, f2).endVertex();
            vertexBuilder.vertex(matrixPose.pose(), (float) (p_194327_ + x), (float) (p_194328_ + y), (float) (p_194329_ + z)).color(r, g, b, a).normal(matrixPose.normal(), f, f1, f2).endVertex();
        });
    }

    @Override
    public void render(DrawerControllerTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof LinkingToolItem) {
            CompoundNBT controllerNBT = stack.getOrCreateTag().getCompound(NBT_CONTROLLER);
            BlockPos controller = new BlockPos(controllerNBT.getInt("X"), controllerNBT.getInt("Y"), controllerNBT.getInt("Z"));
            if (!controller.equals(tile.getBlockPos())) return;
            if (stack.getOrCreateTag().contains(NBT_FIRST)) {
                CompoundNBT firstpos = stack.getOrCreateTag().getCompound(NBT_FIRST);
                BlockPos firstPos = new BlockPos(firstpos.getInt("X"), firstpos.getInt("Y"), firstpos.getInt("Z"));
                RayTraceResult result = RayTraceUtils.rayTraceSimple(Minecraft.getInstance().level, Minecraft.getInstance().player, 8, partialTicks);
                if (result.getType() == RayTraceResult.Type.BLOCK) {
                    BlockPos hit = ((BlockRayTraceResult) result).getBlockPos();
                    AxisAlignedBB aabb = new AxisAlignedBB(Math.min(firstPos.getX(), hit.getX()), Math.min(firstPos.getY(), hit.getY()), Math.min(firstPos.getZ(), hit.getZ()), Math.max(firstPos.getX(), hit.getX()) + 1, Math.max(firstPos.getY(), hit.getY()) + 1, Math.max(firstPos.getZ(), hit.getZ()) + 1);
                    VoxelShape shape = VoxelShapes.create(aabb);
                    renderShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -controller.getX(), -controller.getY(), -controller.getZ(), 1f, 1f, 1f, 1f);
                    return;
                }
            }
            VoxelShape shape = tile.getConnectedDrawers().getCachedVoxelShape();
            if (shape == null || tile.getLevel().getGameTime() % 400 == 0) {
                tile.getConnectedDrawers().rebuildShapes();
                shape = tile.getConnectedDrawers().getCachedVoxelShape();
            }
            //LevelRenderer.renderVoxelShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 1f, 1f, 1f, 1f);
            List<AxisAlignedBB> list = shape.toAabbs();
            int i = MathHelper.ceil((double) list.size() / 3.0D);

            for (int j = 0; j < list.size(); ++j) {
                AxisAlignedBB aabb = list.get(j);
                float f = ((float) j % (float) i + 1.0F) / (float) i;
                float f1 = (float) (j / i);
                float f2 = 1;
                float f3 = 1;
                float f4 = 1;
                renderShape(matrixStack, bufferIn.getBuffer(TYPE), VoxelShapes.create(aabb.move(0.0D, 0.0D, 0.0D)), -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), f2, f3, f4, 1.0F);
            }
        }

    }

    @Override
    public boolean shouldRenderOffScreen(DrawerControllerTile p_112306_) {
        return true;
    }
}
