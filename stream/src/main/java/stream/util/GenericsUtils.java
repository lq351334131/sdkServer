package stream.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/11 19:26
 */
public class GenericsUtils {

    public static List<Field> getAllFields(Object object){
        Class clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null){
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
       // Field[] fields = new Field[fieldList.size()];
        //fieldList.toArray(fields);
        return fieldList;
    }
}
