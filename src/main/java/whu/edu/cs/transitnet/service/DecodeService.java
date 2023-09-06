package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;

@Service
public class DecodeService {
    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    EncodeService encodeService;

    // decode grid


    /**
     * decode cube
     * @param zorder
     * @param level
     * @return
     */
    public int[] decodeZ3(int zorder, int level) {
        int resolution = hytraEngineManager.getParams().getResolution();
        int digits = 3 * resolution;

        String bits;
        String ZERO = "0";
        for(bits = Integer.toBinaryString(zorder); digits > bits.length(); bits = ZERO + bits) {
        }

        String bitsI = "";
        String bitsJ = "";
        String bitsK = "";

        int i;
        for(i = 0; i < bits.length(); ++i) {
            if (i % 3 == 0) {
                bitsK = bitsK + bits.charAt(i);
            }

            if (i % 3 == 1) {
                bitsJ = bitsJ + bits.charAt(i);
            }

            if (i % 3 == 2) {
                bitsI = bitsI + bits.charAt(i);
            }
        }

        i = encodeService.bitToint(bitsI);
        int J = encodeService.bitToint(bitsJ);
        int K = encodeService.bitToint(bitsK);
        int i1 = i * (int)Math.pow(8.0D, (double)level);
        int i2 = i1 + (int)Math.pow(8.0D, (double)level) - 1;
        int j1 = J * (int)Math.pow(8.0D, (double)level);
        int j2 = j1 + (int)Math.pow(8.0D, (double)level) - 1;
        int k1 = K * (int)Math.pow(8.0D, (double)level);
        int k2 = k1 + (int)Math.pow(8.0D, (double)level) - 1;
        return new int[]{i1, i2, j1, j2, k1, k2};
    }
}
