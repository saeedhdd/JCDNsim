package entities.logical;

import java.util.Random;

/**
 * Created by hd on 2018/3/31 AD.
 */
public class DefaultValues {
    public static  int CACHE_SIZE = 100000;        // 100 GB , 100000 MB
    public static  float REQUEST_SIZE = (float) 0.5;
    public static  float SERVICE_TIME = 15f;
    public static  float PSS_PROBABILITY = 0.5f;
    public static float WMC_ALPHA = 0.5f;
    public static  int MCS_DELTA = 3;
    public static  final Random random = new Random();


    public static final float TIME_OUT = 20f ;
    public static final boolean IS_TIME_OUT_ACTIVATED = false ;



    public static final boolean LOGGER_ON = true ;
}
