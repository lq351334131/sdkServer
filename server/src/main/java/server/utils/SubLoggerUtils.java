package server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用分文件模块写入日志
 *
 * @author yi.chen
 * @time 2020/07/10
 */
public class SubLoggerUtils {
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

    public enum LogFileName {

        //配置到logback.xml中的logger name="vipUser"
//        XJK_USER("xjkUser"),
        BAITIAO_USER("newLogger");

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
                if (null != item && item.logFileName == null || item.logFileName.trim().isEmpty()) {
                    return item;
                }
            }
            return null;
        }
    }
}


