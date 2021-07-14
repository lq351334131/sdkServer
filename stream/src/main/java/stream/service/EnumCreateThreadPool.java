package stream.service;

import java.util.concurrent.*;

/**
 * @Author qi.li
 * @Date 2021/7/7 18:06
 */
public enum EnumCreateThreadPool {

    threadPoolInstance;

    private  static ThreadPoolExecutor tpe;
    static {

        tpe=new ThreadPoolExecutor(
                10,
                10,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public Future<?> submit(Runnable runnable) {
        return tpe.submit(runnable);
    }

    public void execute(Runnable runnable) {
        tpe.execute(runnable);
    }
}
