package io.brainergy.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, MapperFeature, PropertyNamingStrategies}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.brainergy.definitions.Ref

trait BJson {

  private val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .serializationInclusion(Include.NON_NULL)
    .build()

  def toOption[T](body: String, ref: Ref[T]): Option[T] = try {
    Option(mapper.readValue(body, ref))
  } catch {
    case t: Throwable => None
  }

  def toJsonString(a: Any):String = mapper.writeValueAsString(a)
}
