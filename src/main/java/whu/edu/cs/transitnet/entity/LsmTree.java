package whu.edu.cs.transitnet.entity;

import java.util.ArrayList;

public class LsmTree {
    private boolean isEmpty;
    private int memSize;

    public ArrayList<Integer> diskSizes;

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public int getMemSize() {
        return memSize;
    }

    public void setMemSize(int memSize) {
        this.memSize = memSize;
    }

    public ArrayList<Integer> getDiskSizes() {
        return diskSizes;
    }

    public void setDiskSizes(ArrayList<Integer> diskSizes) {
        this.diskSizes = diskSizes;
    }

    public static LsmTree parse(String str) {
        LsmTree tree = new LsmTree();
        tree.diskSizes = new ArrayList<>();
        if ("empty".equals(str) || str == null) {
            tree.isEmpty = true;
            tree.memSize = 0;
        }
        String[] kvs = str.split(",");
        String[] memKvs = kvs[0].split(":");
        tree.isEmpty = false;
        tree.memSize = Integer.parseInt(memKvs[1]);
        for (int i = 1; i < kvs.length; i++) {
            String[] diskKvs = kvs[i].split(":");
            tree.diskSizes.add(Integer.parseInt(diskKvs[1]));
        }
        return tree;
    }
}
