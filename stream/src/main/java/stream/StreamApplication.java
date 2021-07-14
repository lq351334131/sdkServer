package stream;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author qi.li
 * @create 2020/11/9 17:36
 */

@SpringBootApplication
@MapperScan(basePackages = {"com.etocrm.sdk.stream.dao"})
public class StreamApplication {

    public static void main(String[] args) {
            SpringApplication.run(StreamApplication.class, args);
        }
}
