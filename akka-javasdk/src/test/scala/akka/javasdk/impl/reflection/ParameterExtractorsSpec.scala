/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.impl.reflection

import akka.javasdk.JsonSupport
import akka.javasdk.impl.ComponentDescriptor
import akka.javasdk.impl.InvocationContext
import akka.javasdk.impl.JsonMessageCodec
import scala.reflect.ClassTag

import com.google.protobuf.ByteString
import com.google.protobuf.DynamicMessage
import com.google.protobuf.any.{ Any => ScalaPbAny }
import com.google.protobuf.{ Any => JavaPbAny }
import akka.javasdk.impl.reflection.ParameterExtractors.BodyExtractor
import akka.javasdk.testmodels.action.EchoAction
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ParameterExtractorsSpec extends AnyWordSpec with Matchers {

  def descriptorFor[T](implicit ev: ClassTag[T]): ComponentDescriptor =
    ComponentDescriptor.descriptorFor(ev.runtimeClass, new JsonMessageCodec)

  "BodyExtractor" should {

    "extract json payload from Any" in {
      val componentDescriptor = descriptorFor[EchoAction]
      val method = componentDescriptor.commandHandlers("StringMessage")

      val jsonBody = JsonSupport.encodeJson("test")

      val field = method.requestMessageDescriptor.findFieldByNumber(1)
      val message = DynamicMessage
        .newBuilder(method.requestMessageDescriptor)
        .setField(field, jsonBody)
        .build()

      val wrappedMessage = ScalaPbAny().withValue(message.toByteString)

      val javaMethod = method.methodInvokers.values.head
      val bodyExtractor: BodyExtractor[_] =
        javaMethod.parameterExtractors.collect { case extractor: BodyExtractor[_] => extractor }.head

      val context = InvocationContext(wrappedMessage, method.requestMessageDescriptor)
      bodyExtractor.extract(context)

    }

    "reject non json payload" in {
      val componentDescriptor = descriptorFor[EchoAction]

      val method = componentDescriptor.commandHandlers("StringMessage")

      val nonJsonBody =
        JavaPbAny
          .newBuilder()
          .setTypeUrl("something.empty")
          .setValue(ByteString.EMPTY)
          .build()

      val field = method.requestMessageDescriptor.findFieldByNumber(1)
      val message = DynamicMessage
        .newBuilder(method.requestMessageDescriptor)
        .setField(field, nonJsonBody)
        .build()

      val wrappedMessage = ScalaPbAny().withValue(message.toByteString)
      val javaMethod = method.methodInvokers.values.head
      val bodyExtractor: BodyExtractor[_] =
        javaMethod.parameterExtractors.collect { case extractor: BodyExtractor[_] => extractor }.head

      val context = InvocationContext(wrappedMessage, method.requestMessageDescriptor)

      intercept[IllegalArgumentException] {
        bodyExtractor.extract(context)
      }

    }
  }

}
