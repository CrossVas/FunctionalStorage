package com.buuz135.functionalstorage.client.loader;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class IQuadTransformer {

    public static int STRIDE = DefaultVertexFormats.BLOCK.getIntegerSize();
    public static int POSITION = findOffset(DefaultVertexFormats.ELEMENT_POSITION);
    public static int COLOR = findOffset(DefaultVertexFormats.ELEMENT_COLOR);
    public static int UV0 = findOffset(DefaultVertexFormats.ELEMENT_UV0);
    public static int UV1 = findOffset(DefaultVertexFormats.ELEMENT_UV1);
    public static int UV2 = findOffset(DefaultVertexFormats.ELEMENT_UV2);
    public static int NORMAL = findOffset(DefaultVertexFormats.ELEMENT_NORMAL);

    private static int findOffset(VertexFormatElement element) {
        int index = DefaultVertexFormats.BLOCK.getElements().indexOf(element);
        return index < 0 ? -1 : DefaultVertexFormats.BLOCK.getOffset(index) / 4;
    }
}
