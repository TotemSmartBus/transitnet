package whu.edu.cs.transitnet.lsm;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LsmConfig {
    private HashMap<String, String> mergeMap;
    private HashMap<Integer, HashSet<String>> keysPerLevel;
    private List<Integer> elementSizeThresholdPerLevel;
    private Integer elementLengthPerLevel;

    public LsmConfig() {
    }

    public HashMap<String, String> getMergeMap() {
        return this.mergeMap;
    }

    public void setMergeMap(HashMap<String, String> mergeMap) {
        this.mergeMap = mergeMap;
    }

    public HashMap<Integer, HashSet<String>> getKeysPerLevel() {
        return this.keysPerLevel;
    }

    public void setKeysPerLevel(HashMap<Integer, HashSet<String>> keysPerLevel) {
        this.keysPerLevel = keysPerLevel;
    }

    public List<Integer> getElementSizeThresholdPerLevel() {
        return this.elementSizeThresholdPerLevel;
    }

    public void setElementSizeThresholdPerLevel(List<Integer> elementSizeThresholdPerLevel) {
        this.elementSizeThresholdPerLevel = elementSizeThresholdPerLevel;
    }

    public Integer getElementLengthPerLevel() {
        return this.elementLengthPerLevel;
    }

    public void setElementLengthPerLevel(Integer elementLengthPerLevel) {
        this.elementLengthPerLevel = elementLengthPerLevel;
    }

    public String saveTo(String filePath, String fileName) throws IOException {
        String fullName = filePath + "/" + fileName;
        File checkFile = new File(fullName);
        if (!checkFile.exists()) {
            checkFile.createNewFile();
        }

        Path path = Paths.get(fullName);
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

        try {
            writer.write("merge_map\n");
            Iterator var7 = this.mergeMap.entrySet().iterator();

            Map.Entry entry;
            while(var7.hasNext()) {
                entry = (Map.Entry)var7.next();
                writer.write((String)entry.getKey());
                writer.write(":");
                writer.write((String)entry.getValue());
                writer.write("\n");
            }

            writer.write("\n");
            writer.write("keys_per_level\n");
            var7 = this.keysPerLevel.entrySet().iterator();

            while(var7.hasNext()) {
                entry = (Map.Entry)var7.next();
                HashSet<String> strings = (HashSet)entry.getValue();
                Iterator var10 = strings.iterator();

                while(var10.hasNext()) {
                    String s = (String)var10.next();
                    writer.write(((Integer)entry.getKey()).toString());
                    writer.write(":");
                    writer.write(s);
                    writer.write("\n");
                }
            }

            writer.write("\n");
            writer.write("element_size_threshold_per_level\n");

            for(int i = 0; i < this.elementSizeThresholdPerLevel.size(); ++i) {
                writer.write(String.valueOf(i));
                writer.write(":");
                writer.write(String.valueOf(this.elementSizeThresholdPerLevel.get(i)));
                writer.write("\n");
            }

            writer.write("\n");
            writer.write("element_length_per_level\n");
            writer.write("all:");
            writer.write(String.valueOf(this.elementLengthPerLevel));
        } catch (Throwable var13) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable var12) {
                    var13.addSuppressed(var12);
                }
            }

            throw var13;
        }

        if (writer != null) {
            writer.close();
        }

        //return fullName;
        ///mnt/c/Users/Dell/Desktop/1/transitnet
        //下面这行是windows下进行调试时使用的：
        String temp=fullName.replace("\\","/").replace(":","").replace("C","c");
        temp="/mnt/"+temp;
        return temp;
    }
}
