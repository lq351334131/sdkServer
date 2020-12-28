package com.etocrm.ck.util

import java.io.InputStreamReader
import java.util.Properties

/**
 * @Author qi.li
 * @create 2020/12/7 10:39
 */
object PropertiesUtil {
  //def main(args: Array[String]): Unit = {
    val properties: Properties = PropertiesUtil.load("config.properties")
    println(properties.getProperty("kafka.broker.list"))


  def load(propertieName:String): Properties ={
    val prop=new Properties();
    prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertieName) , "UTF-8"))
    prop
  }

}
