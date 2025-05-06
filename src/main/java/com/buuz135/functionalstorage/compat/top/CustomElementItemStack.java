//package com.buuz135.functionalstorage.compat.top;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import mcjty.theoneprobe.api.IElement;
//import mcjty.theoneprobe.api.IItemStyle;
//import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
//import mcjty.theoneprobe.network.NetworkTools;
//import mcjty.theoneprobe.rendering.RenderHelper;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.ItemRenderer;
//import net.minecraft.item.ItemStack;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.util.text.TextFormatting;
//
//public class CustomElementItemStack implements IElement {
//
//    private final ItemStack itemStack;
//    private final IItemStyle style;
//    private final String display;
//    private boolean displayName;
//
//    public CustomElementItemStack(ItemStack itemStack, String display, IItemStyle style) {
//        this.itemStack = itemStack;
//        this.style = style;
//        this.display = display;
//        this.displayName = false;
//    }
//
//    public CustomElementItemStack(ItemStack itemStack, String display, IItemStyle style, boolean displayName) {
//        this(itemStack, display, style);
//        this.displayName = true;
//    }
//
//    public CustomElementItemStack(PacketBuffer buf) {
//        if (buf.readBoolean()) {
//            this.itemStack = NetworkTools.readItemStack(buf);
//        } else {
//            this.itemStack = ItemStack.EMPTY;
//        }
//
//        this.style = (new ItemStyle()).width(buf.readInt()).height(buf.readInt());
//        this.display = buf.readUtf();
//        this.displayName = buf.readBoolean();
//    }
//
//    public void render(MatrixStack matrixStack, int x, int y) {
//        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
//        if (!itemStack.isEmpty()) {
//            int size = itemStack.getCount();
//
//            if (!RenderHelper.renderItemStack(Minecraft.getInstance(), itemRender, itemStack, matrixStack, x + (style.getWidth() - 18) / 2, y + (style.getHeight() - 18) / 2, display)) {
//                Minecraft var10000 = Minecraft.getInstance();
//                TextFormatting var10004 = TextFormatting.RED;
//                RenderHelper.renderText(var10000, matrixStack, x, y, var10004 + "ERROR: " + itemStack.getHoverName());
//            }
//            if (this.displayName){
//                RenderHelper.renderText(Minecraft.getInstance(), matrixStack, x + 22, y + 7, itemStack.getHoverName());
//            }
//
//        }
//
//    }
//
//    public int getWidth() {
//        return this.style.getWidth() + (this.displayName ? Minecraft.getInstance().font.width(itemStack.getHoverName().getString()) : 0) ;
//    }
//
//    public int getHeight() {
//        return this.style.getHeight();
//    }
//
//    public void toBytes(PacketBuffer buf) {
//        if (!this.itemStack.isEmpty()) {
//            buf.writeBoolean(true);
//            NetworkTools.writeItemStack(buf, this.itemStack);
//        } else {
//            buf.writeBoolean(false);
//        }
//
//        buf.writeInt(this.style.getWidth());
//        buf.writeInt(this.style.getHeight());
//        buf.writeUtf(this.display);
//        buf.writeBoolean(this.displayName);
//    }
//
//    @Override
//    public int getID() {
//        return 0;
//    }
//}
