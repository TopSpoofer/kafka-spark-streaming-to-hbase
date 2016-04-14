/*
 * Copyright (c) 2015, spoofer, Inc. All Rights Reserved.
 *
 * Author@ spoofer
 *
 * 本程序将json字符串的进行parse, 然后提取其内容,包括:
 * uid: 用户id
 * name: 用户名字
 * age: 用户年龄
 * gender: 用户性别
 * addr: 用户的地址
 * 然后组成UserInfos返回
 */

import org.json4s._
import org.json4s.jackson.JsonMethods._


case class UserInfos(uid: String, name: String, age: String, gender: String, addr: String)

object UserInfosExtractor {
  implicit val formats = DefaultFormats

  @inline
  private def extractUid(js: JValue) = {
    (js \\ "uid").extractOpt[String]
  }

  @inline
  private def extractName(js:JValue) = {
    (js \\ "name").extractOpt[String]
  }

  @inline
  private def extractAge(js: JValue) = {
    (js \\ "age").extractOpt[String]
  }

  @inline
  private def extractGender(js: JValue) = {
    (js \\ "gender").extractOpt[String]
  }

  @inline
  private def extractAddr(js: JValue) = {
    (js \\ "addr").extractOpt[String]
  }

  @inline
  private def parsingUserInfos(userInfosStr: String): Option[JValue] = {
    try {
      Some(parse(userInfosStr))
    } catch {
      case ex: Exception => None
    }
  }

  def extractUserInfos(userInfosStr: String): Option[UserInfos] = {
    this.parsingUserInfos(userInfosStr) match {
      case None => None
      case Some(userInfosJs) =>
        for { //如果有一个为None的话, yield返回的结果为None
          uid <- this.extractUid(userInfosJs)
          name <- this.extractName(userInfosJs)
          age <- this.extractAge(userInfosJs)
          gender <- this.extractGender(userInfosJs)
          addr <- this.extractAddr(userInfosJs)
        } yield UserInfos(uid, name, age, gender, addr)
    }
  }
}
