package com.jemstep.producer

import java.io.File

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.jemstep.commons.Config._
import com.jemstep.commons.DataIO
import com.jemstep.commons.Util._
import com.jemstep.schema_registry.SchemaRegistry._
import com.sksamuel.avro4s.AvroSchema
import org.apache.avro.generic.{GenericArray, GenericData, GenericRecord}
import org.apache.avro.util.Utf8
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

object PlainSinkProducerMain extends App {

  implicit val system = ActorSystem("PlainSinkProducerMain")
  implicit val materializer = ActorMaterializer()

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  implicit val recordWithRecordKeySchema = new GenericData.Record(AvroSchema[RecordKey])
  case class RecordKey(userId: Utf8)

  val producerSettings: ProducerSettings[AnyRef, AnyRef] =
    ProducerSettings(materializer.system,
      new io.confluent.kafka.serializers.KafkaAvroSerializer(client, props),
      new io.confluent.kafka.serializers.KafkaAvroSerializer(client, props))
      .withBootstrapServers(kafka_server)

  println(s"${getClass.getSimpleName} sending messages to $kafka_server")

  val sourceFromFile = Source(DataIO.streamFromFile(new File("./data/test_data.avro")))
    .map { record =>
      ( record.get("userId"),
        record.get("organizationId"),
        record.get("userIdentified"),
        record.get("goalEnvelopeList"),
        record.get("backtestMetrics"),
        record.get("accountHolderDetails"),
        record.get("standingInstruction"),
        record.get("questionnaireEvent")
      )}

  val done = sourceFromFile.collect {
    case ( userId: Utf8,
    organizationId: Utf8,
    userIdentified: GenericRecord,
    goalEnvelopeList : GenericArray[GenericRecord],
    backtestMetrics: GenericRecord,
    accountHolderDetails: GenericRecord,
    standingInstruction: GenericRecord,
    questionnaireEvent: GenericRecord) =>

      val extractedGoalEnvelope: List[GenericRecord] = goalEnvelopeList.asScala.toList.flatMap { goalEnvelope =>
        (goalEnvelope.get("goalEvent"), goalEnvelope.get("portfolioForGoal"), goalEnvelope.get("targetForGoal")) match {
          case (goalEvent: GenericRecord, port: GenericRecord, targ: GenericRecord) => List(goalEvent, port, targ)
          case _ => Nil
        }
      }

      val genericRecords: List[GenericRecord] = List(
        userIdentified,
        backtestMetrics,
        accountHolderDetails,
        standingInstruction,
        questionnaireEvent) ++ extractedGoalEnvelope
      ( userId, organizationId, genericRecords)
  }
    .flatMapConcat { case (userId, organizationId, genericRecords) =>
      val metaRecords = genericRecords.map { record => (userId.toString, organizationId.toString, record) }.filter { x  ⇒
        val fullName = x._3.getSchema.getFullName.trim
        (fullName.equals("com.jemstep.model.events.shared.UserIdentified")) || (fullName.equals( "com.jemstep.model.integration.pershing.StandingInstruction"))
      }
      Source(metaRecords)
    }
    .zipWithIndex.map { case ((userId, organizationId, record), count) =>
    val fullName = record.getSchema.getFullName.trim
    println(s"\n Produced User: $userId, Org: $organizationId, Schema: $fullName\nrecord$count:\n$record\n")

    val key = recordWithRecordKeySchema
    key.put("userId", userId)

    val pRecord = new ProducerRecord[AnyRef, AnyRef]("investor", key, record)
    discard(pRecord.headers().add("userId", userId.getBytes))
    discard(pRecord.headers().add("organizationId", organizationId.getBytes))
    discard(pRecord.headers().add("schema", fullName.getBytes))
    discard(pRecord.headers().add("operation", "UPSERT".getBytes))
    pRecord
  }
    .runWith(Producer.plainSink(producerSettings))

  println("Waiting for producer... ")
  discard(Await.result(done, 50.seconds))
  println("Waiting for producer... done")

  print("Waiting for actor termination... ")
  discard(Await.result(system.terminate, 50.seconds))
  println("done.")

  println("Shutting down JVM")
  System.exit(0)
}