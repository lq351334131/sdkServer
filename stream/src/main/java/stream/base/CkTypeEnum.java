package stream.base;

import com.google.common.base.Enums;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author qi.li
 * @create 2020/11/11 17:42
 */
public enum CkTypeEnum {

    Int64{
        @Override
        Object mockData() {
            return randomDate();
        }
    },Int32{
        @Override
        Object mockData() {
            return (int)((Math.random()*9+1) *10000);
        }
    }, Int16{
        @Override
        Object mockData() {
            return random.nextInt(10);
        }
    }, String{
        @Override
        Object mockData() {
            //java.lang.String s = params.get(random.nextInt(params.size()));
            //s = "Date : [" + DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss") +"]_" + s + counter.getAndIncrement();
            String s="liqi";
            return s;
        }
    },Float64{
        @Override
        Object mockData() {
            float min = 100f;
            float max = 100000f;
            return min + new Random().nextFloat() * (max - min);

        }
    },Boolean{
        @Override
        Object mockData() {
            return random.nextBoolean();
        }
    };
    Boolean nullable;
    Random random = new Random();
    abstract Object mockData();
    public static CkTypeEnum getEnum(String ckType){
        String pre = "Nullable(";
        boolean nullable = true;
        if(ckType.startsWith(pre)){
            nullable = false;
            ckType = StringUtils.substring(ckType,pre.length(),ckType.length()-1);
        }
        CkTypeEnum ckTypeEnum = Enums.getIfPresent(CkTypeEnum.class,ckType).orNull();
        if(ckTypeEnum == null){
            return null;
        }
        ckTypeEnum.nullable = nullable;
        return ckTypeEnum;
    }

    //public static List<String> params = ConfigUtils.getConfig().getStringList("param.string");

    public static AtomicLong counter = new AtomicLong();
    private static long randomDate(){
        try {

            Date end = new Date();
            Date start = DateUtils.addDays(end,-1);

            if(start.getTime() >= end.getTime()){
                return 0L;
            }
            long date = random(start.getTime(),end.getTime());
            return new Date(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long random(long begin,long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }

}
