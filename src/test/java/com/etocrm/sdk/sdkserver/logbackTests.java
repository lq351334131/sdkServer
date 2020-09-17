package com.etocrm.sdk.sdkserver;

import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:application.properties")
public class logbackTests {
    Logger XJK_USER_LOG = LoggerUtils.Logger(LogFileName.XJK_USER);
    Logger BAITIAO_USER_LOG = LoggerUtils.Logger(LogFileName.BAITIAO_USER);

    @Test
    public void testGetBusinessAccount() throws Exception {
        XJK_USER_LOG.info("小金库用户进来了111...");
        BAITIAO_USER_LOG.info("白条用户进来了2222...");
    }
}

 enum LogFileName {

    //配置到logback.xml中的logger name="vipUser"
    XJK_USER("xjkUser"),
    BAITIAO_USER("baitiaoUser");

    private String logFileName;

    LogFileName(String fileName) {
        this.logFileName = fileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public static LogFileName getAwardTypeEnum(String value) {
        LogFileName[] arr = values();
        for (LogFileName item : arr) {
            if (null != item && StringUtils.isNotBlank(item.logFileName)) {
                return item;
            }
        }
        return null;
    }
}

 class LoggerUtils {
    public static <T> Logger Logger(Class<T> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 打印到指定的文件下
     *
     * @param desc 日志文件名称
     * @return
     */
    public static Logger Logger(LogFileName desc) {
        return LoggerFactory.getLogger(desc.getLogFileName());
    }
}
