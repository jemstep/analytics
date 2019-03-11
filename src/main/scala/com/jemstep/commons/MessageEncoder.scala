package com.jemstep.commons

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import org.apache.avro.io.EncoderFactory

import scala.util.Try

object MessageEncoder {

  val schemaIdMap = Map("RecordKey" → 0, "UserIdentified" → 10, "StandingInstruction" → 11)

  def datumToByteArray(schema: Schema, datum: GenericRecord): Option[Array[Byte]] = {
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val outputStream = new ByteArrayOutputStream
    val MAGIC_BYTE = Array[Byte](1)
    val idSize = 4
    Try{
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      val encoder = EncoderFactory.get.directBinaryEncoder(outputStream, null)
      outputStream.write(MAGIC_BYTE)
      for {
        schemaId ← getSchemaID(schema)
      } yield {
        outputStream.write(ByteBuffer.allocate(idSize).putInt(schemaId).array())
        writer.write(datum, encoder)
        encoder.flush()
        val byteData = outputStream.toByteArray
        outputStream.close()
        byteData
      }
    }.fold(throwable => {
      println(
        s"Can't encode proper schema Id for this particular nameSpace: ${schema.getName} as Id(key) is not defined in schemaIdMap: ${schemaIdMap.toString()}. Failed with error: ${throwable.getMessage}");
      None
    },
      x ⇒ x)
  }

  def getSchemaID(schema: Schema): Option[Int] = {
    schemaIdMap.get(schema.getName)
  }

}
