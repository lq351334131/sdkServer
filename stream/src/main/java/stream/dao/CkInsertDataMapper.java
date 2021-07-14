package stream.dao;



import com.eto.sdk.stream.entity.SdkNew;

import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/18 17:55
 */
public interface CkInsertDataMapper {

    Integer batchInsert(List<SdkNew> list);

}
