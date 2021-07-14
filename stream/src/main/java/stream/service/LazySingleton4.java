package stream.service;

/**
 * @Author qi.li
 * @Date 2021/7/1 20:54
 */

/**
 * 以上是双重校验锁失效的原因，不过在JDK1.5之后的版本添加了volatile关键字。
 *
 * 1、volatile的一个语义是禁止指令重排序优化，也就保证了instance变量被赋值的时候对象已经是初始化过的，从而避免了上述问题。
 *
 * 2、Java中的volatile变量是什么？
 *
 *     （1）关键字的作用有两个：
 *
 *  ①多线程主要围绕可见性和原子性两个特性展开，使用volatile关键字修饰的变量，保证了其在多线程之间的可见性，即每次读取到的volatile变量，一定是最新的数据。
 *
 *  ②代码底层执行的顺序是Java代码-->字节码-->根据字节码执行对应的C/C++代码-->C/C++代码被编译成汇编语言-->和硬件电路交互。实际中，为了获取更好的性能，JVM可能会对指令进行重排序，多线程下可能会出现一些意想不到的问题。使用volatile则会禁止语义重排序，也一定程度上降低了代码执行效率。实践角度而言，volatile的一个重要作用就是和CAS结合，保证了原子性。
 *
 *  （2）volatile是一个特殊的修饰符，只有成员变量才能使用它。在Java并发程序缺少同步类的情况下，多线程对成员变量的操作对其他线程是透明的。volatile变量可以保证下一个读取操作会在前一个写操作之后发生。

 */
public class LazySingleton4 {

    private static volatile LazySingleton4 instance=null;


    private LazySingleton4(){}


    public static LazySingleton4 getInstance(){
        if(instance ==null){
            synchronized (LazySingleton4.class){}
            if(instance==null){
                instance=new LazySingleton4();
            }
        }
        return instance;
    }
}
