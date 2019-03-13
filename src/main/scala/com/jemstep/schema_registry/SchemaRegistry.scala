package com.jemstep.schema_registry

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import com.jemstep.commons.Config._

import scala.collection.JavaConverters._


object SchemaRegistry {

  val MAX_SUBJECTS_IN_TOPIC = 1000

  val client = new CachedSchemaRegistryClient(schema_registry_url,MAX_SUBJECTS_IN_TOPIC)

  val props: java.util.Map[String,String] = Map[String,String](
    "key.subject.name.strategy" -> "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
    "value.subject.name.strategy" -> "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
    "schema.registry.url" -> schema_registry_url
  ).asJava

}
