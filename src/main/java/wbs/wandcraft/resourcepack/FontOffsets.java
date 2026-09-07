package wbs.wandcraft.resourcepack;

public record FontOffsets(int ascent, int height, int width, int spaces) {
    public static final FontOffsets DROPPER = new FontOffsets(-3, 54, 54, 1);
    public static final FontOffsets CHEST_3x9 = getChestInstance(3);
    public static final FontOffsets CHEST_6x9 = getChestInstance(6);

    public static FontOffsets getChestInstance(int rows) {
        return new FontOffsets(-4, 18 * rows, 18 * 9, -1);
    }

    public char backgroundOffset() {
        return (char) (ResourcePackBuilder.SPACE_CHAR_ZERO + spaces);
    }
    public char textOffset() {
        return (char) (ResourcePackBuilder.SPACE_CHAR_ZERO - width);
    }
}
