package whu.edu.cs.transitnet;

import org.junit.Test;

public class offsetTest {

    Integer resolution = 2;

    @Test
    public void offsetFuncTest() {
        int size = (int) (Math.pow(8, resolution + 1) - 1) / 7;
        int a = getOffset(8, 0);
        System.out.println("size: " + size);
        System.out.println("offset: " + a);
        int[] b = offsetToZandL(a);
        System.out.println("zorder: " + b[0]);
        System.out.println("level: " + b[1]);


        System.out.println("--------parentOffset--------");
        for (int parentOffset : getAncestorOffsets(a)) {
            System.out.println(parentOffset);
        }
    }

    public int getOffset(int zorder, int level) {
        int base = 0;

        for(int i = 0; i < level; ++i) {
            base += (int)Math.pow(8.0, (double)(resolution - i));
        }

        return base + zorder;
    }

    public int[] offsetToZandL(int offset) {
        int reverse = (int)(Math.pow(8.0, (double)(resolution + 1)) - 1.0) / 7 - (offset + 1);
        int base = 1;

        int level;
        for(level = resolution; reverse / base > 0; --level) {
            reverse -= base;
            base *= 8;
        }

        int z = (int)Math.pow(8.0, (double)(resolution - level)) - (reverse + 1);
        return new int[]{z, level};
    }

    public int[] getAncestorOffsets(int offset) {
        int[] offsets = new int[resolution];
        for (int i = 1; i <= resolution; i++) {
            // i = 1; i - 1 = 0; offset / 8
            // i = 2; i - 1 = 1; offset / 64
            // offsets: 0, 1, ..., resolution - 1 =>
            offsets[i - 1] = getOffset(offset / (int) Math.pow(8, i), i);
        }
        return offsets;
    }
}
