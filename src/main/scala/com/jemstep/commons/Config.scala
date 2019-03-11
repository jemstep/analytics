package com.jemstep.commons

object Config {

  val kafka_host = System.getProperty("kafka.host", "localhost")
  val kafka_port = System.getProperty("kafka.port", "29092").toInt
  val kafka_server = s"$kafka_host:$kafka_port"

  val schema_registry_port = System.getProperty("schema.port", "8082").toInt
  val schema_registry_host = System.getProperty("schema.host", "localhost")
  val schema_registry_url = s"http://$schema_registry_host:$schema_registry_port"


  // Looks like some libraries still insist on using null in weird and wonderful ways
  // https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  val default_avro_reuse = null

}
