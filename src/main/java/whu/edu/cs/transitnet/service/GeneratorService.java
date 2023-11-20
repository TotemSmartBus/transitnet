package whu.edu.cs.transitnet.service;

import edu.whu.hyk.encoding.Decoder;
import edu.whu.hyk.model.PostingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.HistoricalTripIndex;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;
import whu.edu.cs.transitnet.service.index.TripId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.*;


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

    public void setup(String date) throws ParseException {
        this.date=date;
        //反序列化对应日期的txt文件，然后得到对应的cubetriplist...
        historicalTripIndex.cubeTripListSerializationAndDeserilization(date);
        CT_List_arr=historicalTripIndex.getCubeTripList();

        String time_s=date+" 00:00:00";
        String time_e=date+" 23:59:59";

        // 遍历 CT_List_arr 并将其转换为 HashSet 并存储到 CT_List 中
        for (CubeId cubeId : CT_List_arr.keySet()) {
            ArrayList<TripId> tidList = CT_List_arr.get(cubeId);
            HashSet<TripId> tSet = new HashSet<>(tidList);
            CT_List.put(cubeId, tSet);
        }
    }

    public void generateMap() {

        //这里暂只做5.20号的数据且不考虑合并

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
        for(String day : cubeVol.keySet()) {
            BFS(day+sep+0+sep+resolution);
        }
    }




    public HashMap<Integer, HashSet<String>> generatePlanes() {
        bitMap.forEach((day, cubes) -> {
            int size = cubes.length;
            for(int i = 0; i < size; i++){
                if (cubes[i] == 1){
                    //将cube转换Planes
                    int[] zl = offsetToZandL(i);
                    String cid = day+sep+zl[0]+sep+zl[1];
                    int[] box = Decoder.decodeZ3(zl[0],zl[1]);
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
                    }
                }
            }
        });
        return planes;
    }

    /*public void BFS(String cid) {
        String[] items = cid.split(sep);
        String day = items[0];
        int zorder = Integer.parseInt(items[1]);
        int level = Integer.parseInt(items[2]);

        if(level == 0){
            if(!bitMap.containsKey(day)){
                bitMap.put(day, new int[(int) (Math.pow(8,resolution+1) - 1) / 7]);
            }
            bitMap.get(day)[getOffset(zorder,level)] = 1;
            compactionMap.put(cid, cid); return;} //如果到了level 0，就直接写入本身
        if(shouldMerge(cid)) {
            if(!bitMap.containsKey(day)){
                bitMap.put(day, new int[(int) (Math.pow(8,resolution+1) - 1) / 7]);
            }
            bitMap.get(day)[getOffset(zorder,level)] = 1;
            writeMap(cid); return;} //如果应该合并，则写入merge map, bitmap置1
        for(int z  = zorder * 8; z < zorder * 8 + 8; z++){ //否则考察下一层cube
            BFS(day+sep+z+sep+(level-1));
        }

    }*/

    //这个BFS实际上没有进行合并
    public void BFS(String cid) {
        String[] items = cid.split(sep);
        String day = items[0];
        int zorder = Integer.parseInt(items[1]);    //0
        int level = Integer.parseInt(items[2]);     //6

        int[] no_merge_cubes=new int[(int) (Math.pow(8,resolution+1) - 1) / 7];
        for(int i=0;i<Math.pow(8,resolution);i++){
            no_merge_cubes[i]=1;
        }
        bitMap.put(day, no_merge_cubes);

    }

    public boolean shouldMerge(String cid) {
        String[] items = cid.split(sep);
        String day = items[0];
        int zorder = Integer.parseInt(items[1]);
        int level = Integer.parseInt(items[2]);
        int offset = getOffset(zorder, level);
        return cubeVol.get(day)[offset] <= epsilon * Math.pow(5,level);
    }

    public void writeMap(String cid) {
        String[] items = cid.split(sep);
        String day = items[0];
        int zorder = Integer.parseInt(items[1]);
        int l = Integer.parseInt(items[2]);

        if(l == 0){return;}
        for(int z  = zorder * 8; z < zorder * 8 + 8; z++){
            if (cubeVol.get(day)[getOffset(z,l-1)] != 0){
                String ccid = day+sep+z+sep+(l-1);
                compactionMap.put(ccid,cid);
                writeMap(ccid);
            }
        }

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
        int reverse = (int) (Math.pow(8,resolution+1) - 1) / 7 - offset;
        int base = 1;
        int level = resolution;
        while( reverse / base > 0) {
            reverse -= base;
            base *= 8;
            level--;
        }
        int z = (int) Math.pow(8,resolution-level) - reverse;
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

    /**
     * 仅计算输入cube的<b>下一层</b>的cube的offset
     * @param offset
     * @return
     */
    public int[] getChildOffsets(int offset){
        int[] offsets = new int[8];
        for(int i = 0; i < 8; i++){
            offsets[i] = getOffset(offset / (int) Math.pow(8,i), i);
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
}
