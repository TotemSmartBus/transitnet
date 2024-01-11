package whu.edu.cs.transitnet;

import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class randomTest {

    @Test
    public void TimeTest() throws ParseException {


    }

    public int[][] merge(int[][] intervals) {
        int length = intervals.length;;

        if (length == 0) return new int[0][2];

        Arrays.sort(intervals, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });

        List<int[]> merged = new ArrayList<int[]>();
        for (int i = 0; i < length; i++) {
            int l = intervals[i][0];
            int r = intervals[i][1];

            if (merged.size() == 0 || merged.get(merged.size() - 1)[1] < l) {
                merged.add(new int[]{l, r});
            } else {
                merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], r);
            }
        }

        return merged.toArray(new int[merged.size()][]);
    }
}

