package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public class RenderHelper {

    private static final Vector3f ZERO = new Vector3f(0, 0, 0);

    private static final Vector3f[] POS_1 = {
            new Vector3f(0.5f, 0.5f, 0.0005f)
    };

    private static final Vector3f[] POS_2 = {
            new Vector3f(0.5f, 0.25f, 0.0005f),
            new Vector3f(0.5f, 0.75f, 0.0005f)
    };

    private static final Vector3f[] POS_4 = {
            new Vector3f(0.75f, 0.25f, 0.0005f), // Bottom right
            new Vector3f(0.25f, 0.25f, 0.0005f), // Bottom left
            new Vector3f(0.75f, 0.75f, 0.0005f), // Top right
            new Vector3f(0.25f, 0.75f, 0.0005f)  // Top left
    };

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[1] = (color >> 16 & 0xff) / 255f;
        res[2] = (color >> 8 & 0xff) / 255f;
        res[3] = (color & 0xff) / 255f;
        return res;
    }

    public static void renderSlots(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, DrawerTile tile) {
        BigInventoryHandler inventory = (BigInventoryHandler) tile.getStorage();
        DrawerType type = tile.getDrawerType();

        Vector3f[] positions;
        switch (type) {
            case X_1: positions = POS_1; break;
            case X_2: positions = POS_2; break;
            case X_4: positions = POS_4; break;
            default: return;
        }

        float fontScale = type == DrawerType.X_1 ? 0.015f : 0.02f;
        float stackScale = type == DrawerType.X_1 ? 1 : 0.5F;
        float textOffsetY = type == DrawerType.X_1 ? -.155f : 0;

        for (int i = 0; i < positions.length; i++) {
            ItemStack stack = inventory.getStoredStacks().get(i).getStack();
            if (stack.isEmpty()) continue;

            matrixStack.pushPose();
            matrixStack.last().pose().multiply(createTransformMatrix(
                    positions[i], ZERO, new Vector3f(stackScale, stackScale, 1f)));
            renderStack(matrixStack, buffer, light, overlay, stack,
                    inventory.getStackInSlot(i).getCount(), fontScale,
                    tile.getDrawerOptions(), textOffsetY);
            matrixStack.popPose();
        }
    }

    public static void renderStack(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack,
                                   int amount, float scale, ControllableDrawerTile.DrawerOptions options, float textOffsetY) {
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        Quaternion ITEM_LIGHT_ROTATION_3D = Util.make(() -> {
            Quaternion quaternion = new Quaternion(Vector3f.XP, -15f, true);
            quaternion.mul(new Quaternion(Vector3f.YP, 15f, true));
            return quaternion;
        });

        Quaternion ITEM_LIGHT_ROTATION_FLAT = new Quaternion(Vector3f.XP, -45f, true);

        matrixStack.last().pose().multiply(Matrix4f.createScaleMatrix(.5f, .5f, 0.001f));
        boolean render3D = model.isGui3d();
        if (render3D)
            matrixStack.last().normal().mul(ITEM_LIGHT_ROTATION_3D);
        else
            matrixStack.last().normal().mul(ITEM_LIGHT_ROTATION_FLAT);

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, model);
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            matrixStack.pushPose();
            matrixStack.translate(0, textOffsetY, 0);
            renderText(matrixStack, bufferIn, combinedOverlayIn, new StringTextComponent(NumberUtils.getFormatedBigNumber(amount)).withStyle(TextFormatting.WHITE), scale);
            matrixStack.popPose();
        }
    }

    public static void renderSmallStack(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int clotCount, ControllableDrawerTile.DrawerOptions options, float x, float y) {
        matrixStack.pushPose();
        matrixStack.last().pose().multiply(createTransformMatrix(
                new Vector3f(x, y, .0005f), ZERO, new Vector3f(.5f, .5f, 1.0f)));
        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, clotCount, 0.02f, options, 0);
        matrixStack.popPose();
    }

    /* Thanks Mekanism */
    public static void renderText(MatrixStack matrix, IRenderTypeBuffer renderer, int overlayLight, ITextComponent text, float maxScale) {

        matrix.translate(0, -0.65, 0);

        float displayWidth = 1;
        float displayHeight = 1;

        FontRenderer font = Minecraft.getInstance().font;

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        font.drawInBatch(text, offsetX - realWidth / 2, 3 + offsetY - realHeight / 2, overlayLight,
                false, matrix.last().pose(), renderer, false, 0, 0xF000F0);

    }

    public static void renderUpgrades(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, ControllableDrawerTile<?> tile) {
        float scale = 0.0625f;
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
            matrixStack.pushPose();
            matrixStack.translate(0.031, 0.031f, 0.472 / 16D);
            for (int i = 0; i < tile.getStorageUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, scale);
                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
                    matrixStack.popPose();
                    matrixStack.translate(scale, 0, 0);
                }
            }
            matrixStack.popPose();
        }
        if (tile.isVoid()) {
            matrixStack.pushPose();
            matrixStack.last().pose().multiply(createTransformMatrix(
                    new Vector3f(0.969f, 0.031f, 0.469f / 16.0f), ZERO, scale));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalItems.VOID_UPGRADE.get()), ItemCameraTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
            matrixStack.popPose();
        }
    }

    public static void renderFluidSlot(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn,
                                 FluidDrawerTile tile, int slot, double maxHeight, Vector3d translate, boolean halfText, double x2Override) {
        BigFluidHandler inventoryHandler = tile.getFluidHandler();
        FluidStack fluidStack = inventoryHandler.getFluidInTank(slot);
        int displayAmount = fluidStack.getAmount();

        if (fluidStack.isEmpty() && tile.isLocked() && !inventoryHandler.getFilterStack()[slot].isEmpty()) {
            fluidStack = inventoryHandler.getFilterStack()[slot];
            displayAmount = 0;
        }

        if (fluidStack.isEmpty() && displayAmount == 0 && !tile.isLocked()) return;

        matrixStack.pushPose();
        if (translate != null) matrixStack.translate(translate.x, translate.y, translate.z);

        double fillHeight = 1.25 / 16D + (displayAmount / (double) inventoryHandler.getTankCapacity(slot)) * maxHeight;

        AxisAlignedBB bounds = new AxisAlignedBB(
                1 / 16D, 1.25 / 16D, 1 / 16D,
                x2Override, fillHeight, 15 / 16D
        );

        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, fluidStack, displayAmount,
                0.007f, tile.getDrawerOptions(), bounds, halfText);
        matrixStack.popPose();
    }

    public static void renderFluidStack(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLight, int combinedOverlay, FluidStack stack, int amount, float scale, ControllableDrawerTile.DrawerOptions options, AxisAlignedBB bounds, boolean halfText) {
        matrixStack.pushPose();
        ResourceLocation texture = stack.getFluid().getAttributes().getStillTexture(stack);
        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(texture);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.translucent());

        float[] color = RenderHelper.decomposeColorF(stack.getFluid().getAttributes().getColor(stack));
        float red = color[1];
        float green = color[2];
        float blue = color[3];
        float alpha = amount == 0 ? 0.3f : color[0];

        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        double bx1 = bounds.minX * 16;
        double bx2 = bounds.maxX * 16;
        double by1 = bounds.minY * 16;
        double by2 = bounds.maxY * 16;
        double bz1 = bounds.minZ * 16;
        double bz2 = bounds.maxZ * 16;


        Matrix4f posMat = matrixStack.last().pose();

        //TOP

        if (true) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
        }
        //FRONT
        if (true) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(by1);
            float v2 = still.getV(by2);
            builder.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
        }

        matrixStack.popPose();
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.745, 0.97);
            if (halfText) matrixStack.translate(-0.25, 0, 0);
            RenderHelper.renderText(matrixStack, bufferIn, combinedOverlay, new StringTextComponent(NumberUtils.getFormatedFluidBigNumber(amount)).withStyle(TextFormatting.WHITE), scale);
            matrixStack.popPose();
        }
    }
}
