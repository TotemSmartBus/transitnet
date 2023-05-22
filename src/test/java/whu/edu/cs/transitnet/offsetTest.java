package whu.edu.cs.transitnet;

import org.junit.Test;

public class offsetTest {

    Integer resolution = 6;

    @Test
    public void offsetFuncTest() {
        int a = getOffset(18, 4);
        System.out.println(a);
        int[] b = offsetToZandL(a);
        System.out.println(b[0]);
        System.out.println(b[1]);
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
}
