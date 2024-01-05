package whu.edu.cs.transitnet.service;

import whu.edu.cs.transitnet.lsm.LsmConfig;
import edu.whu.hyk.model.PostingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class GeneratorService {
    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    HistoricalTripIndex historicalTripIndex;

    /**
     * key:天
     * value: 当天所有cube的volume，按照zorder和level排序（0-0,1-0,...,63-0, 0-1,1-1,...,7-1, 0-2）
     */
    public  HashMap<String,HashMap<String,Integer>> cubeVol_new = new HashMap<>();
    public  HashMap<String,int[]> cubeVol = new HashMap<>();

    /**
     * 记录合并后的cube分布，如果仍然存在则为1，否则为0
     */
    public  HashMap<String,int[]> bitMap = new HashMap<>();

    public  HashMap<String, String> compactionMap = new HashMap<>();

    /**
     * day -> {plane_idx -> {cube_id}}
     * xplanes: 0 ~ 2^resolution - 1
     * yplanes: 2^resolution ~ 2*2^resolution - 1
     * zplanes: 2*2^resolution ~ 3*2^resolution- 1
     */
    public HashMap<Integer, HashSet<String>> planes = new HashMap<>();

    static int epsilon=30;
    static int resolution=6;
    static String sep="@";

    public HashMap<CubeId, ArrayList<TripId>> CT_List_arr=new HashMap<>();
    public HashMap<CubeId, HashSet<TripId>> CT_List=new HashMap<>();
    public HashMap<CubeId, HashSet<TripId>> merge_CT_List = new HashMap();

    public HashMap<TripId, ArrayList<CubeId>> TC_List_arr=new HashMap<>();
    public HashMap<TripId, ArrayList<CubeId>> merge_TC_List_arr=new HashMap<>();
    private String date;

    public void setup(String date, ConcurrentHashMap h) throws ParseException {
        this.date=date;
        CT_List=new HashMap<>(h);
    }

    public void setup(String date){

    }


    public LsmConfig generateConfig() {
        generateMap();
        generatePlanes();
        LsmConfig config = new LsmConfig();
        config.setMergeMap(compactionMap);
        HashMap<Integer, HashSet<String>> keysPerLevel = new HashMap();
        bitMap.forEach((day, map) -> {
            for(int i = 0; i < map.length; ++i) {
                if (map[i] == 1) {
                    int[] zl = offsetToZandL(i);
                    String cid = day + sep + zl[0] + sep + zl[1];
                    int level = zl[1];
                    HashSet<String> cids = new HashSet();
                    if (keysPerLevel.containsKey(level)) {
                        cids = (HashSet)keysPerLevel.get(level);
                    }

                    cids.add(cid);
                    keysPerLevel.put(zl[1], cids);
                }
            }

        });
        config.setKeysPerLevel(keysPerLevel);
        List<Integer> thresholds = new ArrayList();

        for(int i = 0; i <= resolution; ++i) {
            thresholds.add((int)((double)epsilon * Math.pow(8.0, (double)i)));
        }

        config.setElementSizeThresholdPerLevel(thresholds);
        int elementLength = 20;
        config.setElementLengthPerLevel(Integer.valueOf(elementLength));
        return config;
    }

    public HashMap<Integer, HashSet<TripId>> generateKV() {
        HashMap<Integer, HashSet<TripId>> result = new HashMap();
        Iterator CT_it = CT_List.entrySet().iterator();

        while(true) {
            while(CT_it.hasNext()) {
                Map.Entry<CubeId, HashSet<TripId>> entry = (Map.Entry)CT_it.next();
                String cid = entry.getKey().toString();
                HashSet<TripId> idList = entry.getValue();

                int z = Integer.parseInt(cid);
                int l = 0;
                if (((int[])bitMap.get(date))[getOffset(z, l)] == 1) {
                    result.put(z, idList);
                } else {
                    String destination;
                    for(destination = cid; compactionMap.containsKey(destination); destination = compactionMap.get(destination)) {
                    }

                    HashSet<TripId> newIdList = new HashSet();
                    if (result.containsKey(destination)) {
                        newIdList = result.get(destination);
                    }

                    newIdList.addAll(idList);
                    result.put(Integer.parseInt(destination), newIdList);
                }
            }

            return result;
        }
    }


    public void generateMap() {
        //计算cube volume
        CT_List.forEach((cid, idList) -> {
            int zorder = Integer.parseInt(cid.toString());
            int level = 0;

            if(!cubeVol.containsKey(date)){
                int size = (int) (Math.pow(8,resolution+1) - 1) / 7;
                cubeVol.put(date, new int[size]);
            }
            int offset  = getOffset(zorder,level);
            try {
                cubeVol.get(date)[offset] += idList.size();
            } catch (Exception e){
                System.out.println("Generate map error:  Arrays.toString(dzl)");
            }
            for (int parentOffset : getAncestorOffsets(offset)) {
                cubeVol.get(date)[parentOffset] += idList.size();
            }
        });

        //生成compaction map
        setMap();
    }


    public void setMap(){
        int[] no_merge_cubes=new int[(int) (Math.pow(8,resolution+1) - 1) / 7];
        CT_List.forEach((cid, idList) -> {
            int zorder = Integer.parseInt(cid.toString());
            int level = 0;
            String fullCid=date+sep+zorder+sep+level;
            compactionMap.put(fullCid,fullCid);
            no_merge_cubes[zorder]=1;
        });
        bitMap.put(date, no_merge_cubes);
    }

    public HashMap<Integer, HashSet<String>> generatePlanes() {
        bitMap.forEach((day, cubes) -> {
            int size = cubes.length;
            for(int i = 0; i < size; i++){
                if (cubes[i] == 1){
                    //将cube转换Planes
                    //这个OFFSET的cube代表的是哪个（DZL）cube，根据ZL算出他的start-end的最小粒度的三维坐标（0-64）
                    int[] zl = offsetToZandL(i);
                    String cid = day+sep+zl[0]+sep+zl[1];
                    int[] box = decodeZ3(zl[0],zl[1]);
                    for (int a = box[0]; a <= box[1]; a++){
                        if(!planes.containsKey(a)){planes.put(a, new HashSet<>());}
                        planes.get(a).add(cid);
                    }
                    for (int b = box[2]; b <= box[3]; b++){
                        int idx = b + (int) Math.pow(2, resolution);
                        if(!planes.containsKey(idx)){planes.put(idx, new HashSet<>());}
                        planes.get(idx).add(cid);
                    }
                    for (int c = box[4]; c <= box[5]; c++){
                        int idx = c + (int) Math.pow(2, resolution+1);
                        if(!planes.containsKey(idx)){planes.put(idx, new HashSet<>());}
                        planes.get(idx).add(cid);
                    }//遍历所有不同level的cube，然后计算这个cube的最↙和最↗的投影到ijk上的坐标，然后计算每个轴的每个单元格上投了哪些cube
                }
            }
        });
        return planes;
    }

    public void updateMergeCTandTC(){
        merge_CT_List=CT_List;
    }

    /**
     * 根据zorder和level计算在cubeVol中的offset
     * @param zorder
     * @param level
     * @return
     */
    public int getOffset(int zorder, int level){
        int base = 0;
        for(int i = 0; i < level; i++){
            base += (int) Math.pow(8,resolution - i);
        }
        return base + zorder;
    }

    public int[] offsetToZandL(int offset) {
        int reverse = (int) (Math.pow(8.0,(double) resolution+1) - 1.0) / 7 - (offset+1);
        int base = 1;


        int level ;
        for(level=resolution;reverse/base>0;--level){
            reverse-=base;
            base*=8;
        }
        int z = (int) Math.pow(8.0,(double) resolution-level) - (reverse+1);
        return new int[]{z,level};
    }


    /**
     * 计算level0cube的<b>所有</b>上层cube的offset
     * @param offset
     * @return
     */
    public int[] getAncestorOffsets(int offset){
        int[] offsets = new int[resolution];
        for(int i = 1; i <= resolution; i++){
            offsets[i-1] = getOffset(offset / (int) Math.pow(8,i), i);
        }
        return offsets;
    }

    public void writeLsmConfig(String filePath){
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");


            StringBuilder mm = new StringBuilder("merge map\n");
            writer.write(mm.toString());
            compactionMap.forEach((k,v) -> {
                try {
                    writer.write(""+k+":"+v+"\n");
                }catch (IOException e){
                    e.printStackTrace();
                }
            });



            StringBuilder kpl = new StringBuilder("\nkeys_per_level\n");
            bitMap.forEach((day,map) -> {
                for(int i = 0; i < map.length; i++){
                    if(map[i] == 1) {
                        int[] zl = offsetToZandL(i);
                        String cid = day+sep+zl[0]+sep+zl[1];
                        kpl.append(zl[1]).append(":").append(cid).append("\n");
                    }
                }
                kpl.append(day).append(sep).append(0).append(sep).append(resolution);
            });

            writer.write(kpl.toString());

            StringBuilder estpl = new StringBuilder("\nelement_size_threshold_per_level\n");
            for(int i = 0; i <= resolution; i++){
                estpl.append(i).append(":").append((int) (epsilon * Math.pow(5, i))).append("\n");
            }
            writer.write(estpl.toString());

            writer.write("\nelement_length_per_level\n");
            writer.write("all:"+10);
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeKV(String filePath){
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");



            PostingList.CP.forEach((cid, idList)->{
                StringBuilder kv = new StringBuilder();
                String[] items = cid.split(sep);
                String day = items[0];
                int z = Integer.parseInt(items[1]);
                int l = Integer.parseInt(items[2]);

                //如果不用合并，直接写入
                if (bitMap.get(day)[getOffset(z,l)] == 1){
                    idList.forEach(id -> {
                        kv.append("put:").append(cid).append(",").append(id).append("\n");
                    });
                }

                else {
                    String destination = cid;
                    while (compactionMap.containsKey(destination)){
                        destination = compactionMap.get(destination);
                    }
                    String finalDestination = destination;
                    idList.forEach(id -> {
                        kv.append("put:").append(finalDestination).append(",").append(id).append("\n");
                    });
                }
                try {
                    writer.write(kv.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTCWithCompaction(String filePath) {
        HashMap<String,HashSet<Integer>> CT = new HashMap<>();
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            //通过合并前的ct和compaction map 构造合并后的ct
            PostingList.CT.forEach((cid, tid_set) -> {
                String to_cid = cid;
                while(compactionMap.containsKey(to_cid)){
                    to_cid = compactionMap.get(to_cid);
                }
                if(!CT.containsKey(to_cid)){
                    CT.put(to_cid, new HashSet<>());
                }
                CT.get(to_cid).addAll(PostingList.CT.get(cid));

            });

            CT.entrySet().forEach(entry -> {
                try {
                    writer.write(entry.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean testMap() {
        for(int i=0;i<bitMap.get("2023-05-20").length;i++){
            if(bitMap.get("2023-05-20")[i]==1){
                int[] zl=offsetToZandL(i);
                if(!merge_CT_List.containsKey(new CubeId("2023-05-20@"+zl[0]+"@"+zl[1]))){
                    return false;
                }
            }
        }
        return true;
    }

    public int[] decodeZ3(int zorder, int level) {
        int digits = 3 * resolution;

        String bits;
        for(bits = Integer.toBinaryString(zorder); digits > bits.length(); bits = "0" + bits) {
        }

        String bitsI = "";
        String bitsJ = "";
        String bitsK = "";

        int i;
        for(i = 0; i < bits.length(); ++i) {
            if (i % 3 == 0) {
                bitsI = bitsI + bits.charAt(i);
            }

            if (i % 3 == 1) {
                bitsJ = bitsJ + bits.charAt(i);
            }

            if (i % 3 == 2) {
                bitsK = bitsK + bits.charAt(i);
            }
        }

        i = bitToint(bitsI);
        int J = bitToint(bitsJ);
        int K = bitToint(bitsK);
        int i1 = i * (int)Math.pow(2, (double)level);
        int i2 = i1 + (int)Math.pow(2, (double)level) - 1;
        int j1 = J * (int)Math.pow(2, (double)level);
        int j2 = j1 + (int)Math.pow(2, (double)level) - 1;
        int k1 = K * (int)Math.pow(2, (double)level);
        int k2 = k1 + (int)Math.pow(2, (double)level) - 1;
        return new int[]{i1, i2, j1, j2, k1, k2};
    }

    public int bitToint(String bits) {
        int sum = 0;
        int length = bits.length();

        for(int i = 0; i < length; ++i) {
            sum = (int)((double)sum + (double)Integer.parseInt(String.valueOf(bits.charAt(i))) * Math.pow(2.0, (double)(length - i - 1)));
        }

        return sum;
    }
}
