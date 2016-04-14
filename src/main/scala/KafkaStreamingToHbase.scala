/*
 * Copyright (c) 2015, spoofer, Inc. All Rights Reserved.
 *
 * Author@ spoofer
 *
 * 本程序提供读取kafka数据,然后将这些数据解析后存入hbase.
 *
 * 对于hbase集群的配置在src/resources/hbase-site.xml中指定.
 *
 * 如果你需要运行本程序, 你需要根据你实际情况配置KafkaStreamingToHbase中的 zkQuorum, topics, brokers.
 *
 * zkQuorum: zookeeper集群地址
 *
 * topics: kafka的topic, 你的streaming程序会从这个topic中读取数据
 *
 * brokers: kafka集群的地址
 *
 * 本程序用的数据格式为: {"uid":"123", "name":"spoofer", "age":"23", "gender":"M", "addr":"guangzhou"}
 *
 * 运行本程序之前,请确保你的hbase集群上没有 KafkaToHbase 这张表!
 */

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import top.spoofer.hbrdd.config.HbRddConfig
import top.spoofer.hbrdd._

object KafkaStreamingToHbase {
  private val sparkMaster = "Core1"
  private val sparkMasterPort = "7077"
  private val zkQuorum = "Core1:2181,Core2:2181,Core3:2181"
  private val group = "test"
  private val topics = "users"
  private val numThreads = "1"
  private val brokers = "Kafka1:9092,Kafka1:9093,Kafka2:9094"

  /**
    * Map(qualifier, value)
    * @param user 用户信息
    * @return
    */
  implicit def userInfoToMap(user: UserInfos): Map[String, String] = {
    Map("name" -> user.name, "age" -> user.age, "gender" -> user.gender, "addr" -> user.addr)
  }

  def main(args: Array[String]) {
    implicit val hbConfig = HbRddConfig()

    TableManager.createTable(force = false)
    println("created table: KafkaToHbase, family is: UserInfos .....")

    // Create context with 5 second batch interval
    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount").setMaster(s"spark://$sparkMaster:$sparkMasterPort")
      .setJars(List("./out/artifacts/spark_streaming_kafka_jar/spark-streaming-kafka.jar"))
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    val usersLine = messages.map(_._2)  //获取数据
    val userInfosData = usersLine map { userInfosStr =>
      UserInfosExtractor.extractUserInfos(userInfosStr) //提取数据内容
    } filter(_.isDefined) map {_.get}

    /**
      * 将提取的内容组成hbrdd库需要的格式
      * rdd(rowid, Map(column, value))
      */
    val dataToHbase = userInfosData map { userInfo =>
      userInfo.uid -> userInfoToMap(userInfo)
    }

    dataToHbase.foreachRDD(rdd => rdd.put2Hbase("KafkaToHbase", "UserInfos"))

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}
