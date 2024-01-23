package io.brainergy.util

import com.typesafe.config.ConfigFactory

import scala.util.Try

object Conf {

  private val conf = ConfigFactory.load()

  def apply(key: String): String = {
    if (System.getenv().containsKey(key)) System.getenv(key) else conf.getString(key)
  }

  def apply(key: String, default: String): String =
    if (System.getenv().containsKey(key))
      System.getenv(key)
    else
      Try(conf.getString(key)).getOrElse(default)

}
