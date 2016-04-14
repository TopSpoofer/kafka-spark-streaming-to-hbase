/*
 * Copyright (c) 2015, spoofer, Inc. All Rights Reserved.
 *
 * Author@ spoofer
 *
 * 本程序提供创建hbase数据表的操作.其中表名字为KafkaToHbase.
 * 这个表只有一个列簇: UserInfos, 其maxversions为100
 * 对于hbase集群的配置在src/resources/hbase-site.xml中指定
 */

import top.spoofer.hbrdd._
import top.spoofer.hbrdd.config.HbRddConfig
import top.spoofer.hbrdd.hbsupport.{FamilyPropertiesStringSetter, HbRddFamily}

object TableManager {
  private val tableName = "KafkaToHbase"

  private def getPropertiesFamilys = {
    /* 修改列簇的maxversions为100 */
    val cf1 = HbRddFamily("UserInfos", FamilyPropertiesStringSetter(Map("maxversions" -> "100")))
    Set(cf1)
  }

  def createTable(force: Boolean = false)(implicit hbRddConfig: HbRddConfig) = {
    val admin = HbRddAdmin.apply()

    if (force) {  //强制建表前需要先删除原来的表
      admin.dropTable(tableName)
    }

    admin.createTableByProperties(tableName, this.getPropertiesFamilys)
    admin.close()
  }
}
