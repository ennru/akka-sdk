/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.impl

import com.google.protobuf.BytesValue
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.empty.Empty
import com.google.protobuf.{ Any => JavaPbAny }
import akka.platform.javasdk.impl.ProtoDescriptorGenerator.fileDescriptorName
import akka.platform.spring.testmodels.action.ActionsTestModels.ActionWithOneParam
import akka.platform.spring.testmodels.action.ActionsTestModels.ActionWithoutParam
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.ActionWithMethodLevelAcl
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.ActionWithMethodLevelAclAndSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.ActionWithServiceLevelAcl
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousDeleteHandlersVESubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersESSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersESTypeLevelSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersStreamTypeLevelSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersTopiSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersTopicTypeLevelSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersVESubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.AmbiguousHandlersVETypeLevelSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForESSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForESTypeLevelSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForStreamSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForTopicSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForTopicTypeLevelSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.DifferentTopicForVESubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.ESWithPublishToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.EventStreamPublishingAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.EventStreamSubscriptionAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.InvalidConsumerGroupsWhenSubscribingToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.InvalidSubscribeToEventSourcedEntityAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.InvalidSubscribeToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingHandlersWhenSubscribeToEventSourcedEntityAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingHandlersWhenSubscribeToEventSourcedOnMethodLevelEntityAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingSourceForTopicPublishing
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForESSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForStreamSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForTopicSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForTopicTypeLevelSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForTypeLevelESSubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MissingTopicForVESubscription
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MultipleTypeLevelSubscriptionsInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.MultipleUpdateMethodsForVETypeLevelSubscriptionInAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.PublishBytesToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.StreamSubscriptionWithPublishToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeOnlyOneToEventSourcedEntityActionTypeLevel
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToBytesFromTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToEventSourcedEmployee
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToEventSourcedEntityAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToTopicCombinedAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToTopicTypeLevelAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToTopicTypeLevelCombinedAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToTwoTopicsAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToValueEntityAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToValueEntityTypeLevelAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.SubscribeToValueEntityWithDeletesAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.TypeLevelESWithPublishToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.TypeLevelTopicSubscriptionWithPublishToTopicAction
import akka.platform.spring.testmodels.subscriptions.PubSubTestModels.VEWithPublishToTopicAction
import akka.platform.spring.testmodels.valueentity.CounterState
import org.scalatest.wordspec.AnyWordSpec

class ActionDescriptorFactorySpec extends AnyWordSpec with ComponentDescriptorSuite {

  "Action descriptor factory" should {

    "validate an Action must be declared as public" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[NotPublicComponents.NotPublicAction]).failIfInvalid
      }.getMessage should include("NotPublicAction is not marked with `public` modifier. Components must be public.")
    }

    "generate mappings for an Action with method without path param" in {
      assertDescriptor[ActionWithoutParam] { desc =>

        val clazz = classOf[ActionWithoutParam]
        desc.fileDescriptor.getName shouldBe fileDescriptorName(clazz.getPackageName, clazz.getSimpleName)

        val methodDescriptor = desc.serviceDescriptor.findMethodByName("Message")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val method = desc.commandHandlers("Message")
        method.requestMessageDescriptor.getFields.size() shouldBe 0
      }
    }

    "generate mappings for an Action with method with one param" in {
      assertDescriptor[ActionWithOneParam] { desc =>

        val methodDescriptor = desc.serviceDescriptor.findMethodByName("Message")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val method = desc.commandHandlers("Message")
        assertRequestFieldJavaType(method, "json_body", JavaType.MESSAGE)
      }
    }

    /* FIXME these should work for endpoints
    "generate mappings for an Action with method level JWT" in {
      assertDescriptor[ActionWithMethodLevelJWT] { desc =>
        val methodDescriptor = findMethodByName(desc, "Message")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val method = desc.commandHandlers("Message")
        val jwtOption = findKalixMethodOptions(methodDescriptor).getJwt
        jwtOption.getBearerTokenIssuer(0) shouldBe "a"
        jwtOption.getBearerTokenIssuer(1) shouldBe "b"
        jwtOption.getValidate(0) shouldBe JwtMethodMode.BEARER_TOKEN
        assertRequestFieldJavaType(method, "json_body", JavaType.MESSAGE)

        val Seq(claim1, claim2, claim3) = jwtOption.getStaticClaimList.asScala.toSeq
        claim1.getClaim shouldBe "roles"
        claim1.getValueList.asScala.toSeq shouldBe Seq("viewer", "editor")
        claim2.getClaim shouldBe "aud"
        claim2.getValue(0) shouldBe "${ENV}.kalix.io"
        claim3.getClaim shouldBe "sub"
        claim3.getPattern shouldBe "^sub-\\S+$"
      }
    }

    "generate mappings for an Action with service level JWT" in {
      assertDescriptor[ActionWithServiceLevelJWT] { desc =>
        val extension = desc.serviceDescriptor.getOptions.getExtension(kalix.Annotations.service)
        val jwtOption = extension.getJwt
        jwtOption.getBearerTokenIssuer(0) shouldBe "a"
        jwtOption.getBearerTokenIssuer(1) shouldBe "b"
        jwtOption.getValidate shouldBe JwtServiceMode.BEARER_TOKEN

        val Seq(claim1, claim2, claim3) = jwtOption.getStaticClaimList.asScala.toSeq
        claim1.getClaim shouldBe "roles"
        claim1.getValueList.asScala.toSeq shouldBe Seq("editor", "viewer")
        claim2.getClaim shouldBe "aud"
        claim2.getValue(0) shouldBe "${ENV}.kalix.io"
        claim3.getClaim shouldBe "sub"
        claim3.getPattern shouldBe "^\\S+$"
      }
    }

     */

    "generate mapping with Event Sourced Subscription annotations" in {
      assertDescriptor[SubscribeToEventSourcedEmployee] { desc =>

        val onUpdateMethodDescriptor = findMethodByName(desc, "KalixSyntheticMethodOnESEmployee")
        onUpdateMethodDescriptor.isServerStreaming shouldBe false
        onUpdateMethodDescriptor.isClientStreaming shouldBe false

        val onUpdateMethod = desc.commandHandlers("KalixSyntheticMethodOnESEmployee")
        onUpdateMethod.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventing = findKalixServiceOptions(desc).getEventing.getIn
        eventing.getEventSourcedEntity shouldBe "employee"

        // in case of @Migration, it should map 2 type urls to the same method
        onUpdateMethod.methodInvokers.view.mapValues(_.method.getName).toMap should
        contain only ("json.kalix.io/created" -> "methodOne", "json.kalix.io/old-created" -> "methodOne", "json.kalix.io/emailUpdated" -> "methodTwo")
      }
    }

    "generate combined mapping with Event Sourced Entity Subscription annotation" in {
      assertDescriptor[SubscribeToEventSourcedEntityAction] { desc =>
        val methodDescriptor = findMethodByName(desc, "KalixSyntheticMethodOnESCounterentity")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnESCounterentity")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixMethodOptions(methodDescriptor).getEventing.getIn
        eventSourceOne.getEventSourcedEntity shouldBe "counter-entity"
      }
    }

    "generate mapping with Value Entity Subscription annotations" in {
      assertDescriptor[SubscribeToValueEntityAction] { desc =>

        val onUpdateMethodDescriptor = findMethodByName(desc, "OnUpdate")
        onUpdateMethodDescriptor.isServerStreaming shouldBe false
        onUpdateMethodDescriptor.isClientStreaming shouldBe false

        val onUpdateMethod = desc.commandHandlers("OnUpdate")
        onUpdateMethod.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventing = findKalixMethodOptions(onUpdateMethodDescriptor).getEventing.getIn
        eventing.getValueEntity shouldBe "ve-counter"

        // in case of @Migration, it should map 2 type urls to the same method
        onUpdateMethod.methodInvokers should have size 2
        onUpdateMethod.methodInvokers.values.map { javaMethod =>
          javaMethod.parameterExtractors.length shouldBe 1
        }
        onUpdateMethod.methodInvokers.view.mapValues(_.method.getName).toMap should
        contain only ("json.kalix.io/counter-state" -> "onUpdate", "json.kalix.io/" + classOf[
          CounterState].getName -> "onUpdate")
      }
    }

    "generate mapping with Value Entity Subscription annotations (type level)" in {
      assertDescriptor[SubscribeToValueEntityTypeLevelAction] { desc =>

        val onUpdateMethodDescriptor = findMethodByName(desc, "OnUpdate")
        onUpdateMethodDescriptor.isServerStreaming shouldBe false
        onUpdateMethodDescriptor.isClientStreaming shouldBe false

        val onUpdateMethod = desc.commandHandlers("OnUpdate")
        onUpdateMethod.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventing = findKalixServiceOptions(desc).getEventing.getIn
        eventing.getValueEntity shouldBe "ve-counter"

        // in case of @Migration, it should map 2 type urls to the same method
        onUpdateMethod.methodInvokers should have size 2
        onUpdateMethod.methodInvokers.values.map { javaMethod =>
          javaMethod.parameterExtractors.length shouldBe 1
        }
        onUpdateMethod.methodInvokers.view.mapValues(_.method.getName).toMap should
        contain only ("json.kalix.io/counter-state" -> "onUpdate", "json.kalix.io/" + classOf[
          CounterState].getName -> "onUpdate")
      }
    }

    "generate mapping with Value Entity and delete handler" in {
      assertDescriptor[SubscribeToValueEntityWithDeletesAction] { desc =>

        val onUpdateMethodDescriptor = findMethodByName(desc, "OnUpdate")
        onUpdateMethodDescriptor.isServerStreaming shouldBe false
        onUpdateMethodDescriptor.isClientStreaming shouldBe false

        val onUpdateMethod = desc.commandHandlers("OnUpdate")
        onUpdateMethod.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventing = findKalixMethodOptions(onUpdateMethodDescriptor).getEventing.getIn
        eventing.getValueEntity shouldBe "ve-counter"
        eventing.getHandleDeletes shouldBe false

        val onDeleteMethodDescriptor = findMethodByName(desc, "OnDelete")
        onDeleteMethodDescriptor.isServerStreaming shouldBe false
        onDeleteMethodDescriptor.isClientStreaming shouldBe false

        val onDeleteMethod = desc.commandHandlers("OnDelete")
        onDeleteMethod.requestMessageDescriptor.getFullName shouldBe Empty.javaDescriptor.getFullName

        val deleteEventing = findKalixMethodOptions(onDeleteMethodDescriptor).getEventing.getIn
        deleteEventing.getValueEntity shouldBe "ve-counter"
        deleteEventing.getHandleDeletes shouldBe true
      }
    }

    "generate mapping for an Action with a subscription to a topic" in {
      assertDescriptor[SubscribeToTopicAction] { desc =>
        val methodOne = desc.commandHandlers("MessageOne")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixMethodOptions(desc, "MessageOne").getEventing.getIn
        eventSourceOne.getTopic shouldBe "topicXYZ"
        eventSourceOne.getConsumerGroup shouldBe "cg"

        // should have a default extractor for any payload
        val javaMethod = methodOne.methodInvokers.values.head
        javaMethod.parameterExtractors.length shouldBe 1
      }
    }

    "generate mapping for an Action with a subscription to a topic (type level)" in {
      assertDescriptor[SubscribeToTopicTypeLevelAction] { desc =>
        val methodOne = desc.commandHandlers("MessageOne")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixMethodOptions(desc, "MessageOne").getEventing.getIn
        eventSourceOne.getTopic shouldBe "topicXYZ"
        eventSourceOne.getConsumerGroup shouldBe "cg"

        // should have a default extractor for any payload
        val javaMethod = methodOne.methodInvokers.values.head
        javaMethod.parameterExtractors.length shouldBe 1
      }
    }

    "generate mapping for an Action with a subscription to a topic (type level) with combined handler" in {
      assertDescriptor[SubscribeToTopicTypeLevelCombinedAction] { desc =>
        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnTopicTopicXYZ")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val topicSource = findKalixMethodOptions(desc, "KalixSyntheticMethodOnTopicTopicXYZ").getEventing.getIn
        topicSource.getTopic shouldBe "topicXYZ"
        topicSource.getConsumerGroup shouldBe "cg"
        // we don't set the property so the proxy won't ignore. Ignore is only internal to the SDK
        topicSource.getIgnore shouldBe false
        topicSource.getIgnoreUnknown shouldBe false

        // should have a default extractor for any payload
        val javaMethod = methodOne.methodInvokers.values.head
        javaMethod.parameterExtractors.length shouldBe 1
      }
    }

    "generate mapping for an Action with a subscription to a topic with combined handler" in {
      assertDescriptor[SubscribeToTopicCombinedAction] { desc =>
        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnTopicTopicXYZ")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnTopicTopicXYZ").getEventing.getIn
        eventSourceOne.getTopic shouldBe "topicXYZ"
        eventSourceOne.getConsumerGroup shouldBe "cg"

        // should have a default extractor for any payload
        val javaMethod = methodOne.methodInvokers.values.head
        javaMethod.parameterExtractors.length shouldBe 1
      }
    }

    "build a combined synthetic method when there are two subscriptions to the same topic" in {
      assertDescriptor[SubscribeToTwoTopicsAction] { desc =>
        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnTopicTopicXYZ")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnTopicTopicXYZ").getEventing.getIn
        eventSourceOne.getTopic shouldBe "topicXYZ"

        methodOne.methodInvokers.size shouldBe 4

        val javaMethodNames = methodOne.methodInvokers.values.map(_.method.getName)
        javaMethodNames should contain("methodOne")
        javaMethodNames should contain("methodTwo")
        javaMethodNames should contain("methodThree")
      }
    }

    "generate mapping with Event Sourced Entity Subscription annotation type level with only one method" in {
      assertDescriptor[SubscribeOnlyOneToEventSourcedEntityActionTypeLevel] { desc =>
        val methodDescriptor = findMethodByName(desc, "MethodOne")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val methodOne = desc.commandHandlers("MethodOne")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventSourceOne = findKalixServiceOptions(desc).getEventing.getIn
        eventSourceOne.getEventSourcedEntity shouldBe "counter-entity"
        // we don't set the property so the proxy won't ignore. Ignore is only internal to the SDK
        eventSourceOne.getIgnore shouldBe false
        eventSourceOne.getIgnoreUnknown shouldBe false
      }
    }

    "validates it is forbidden Entity Subscription at annotation type level and method level at the same time" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[InvalidSubscribeToEventSourcedEntityAction]).failIfInvalid
      }.getMessage should include(
        "You cannot use @Consume.FromEventSourcedEntity annotation in both methods and class.")
    }

    "validates that ambiguous handler VE" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersVESubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that ambiguous delete handler VE" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousDeleteHandlersVESubscriptionInAction]).failIfInvalid
      }.getMessage should include("Ambiguous delete handlers: [methodOne, methodTwo].")
    }

    "validates that ambiguous handler VE (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersVETypeLevelSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that only single update handler is present for VE sub (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MultipleUpdateMethodsForVETypeLevelSubscriptionInAction]).failIfInvalid
      }.getMessage should include("Duplicated update methods [methodOne, methodTwo]for ValueEntity subscription.")
    }

    "validates that only type level subscription is valid" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MultipleTypeLevelSubscriptionsInAction]).failIfInvalid
      }.getMessage should include("Only one subscription type is allowed on a type level.")
    }

    "validates that ambiguous handler ES" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersESSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that ambiguous handler ES (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersESTypeLevelSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that ambiguous handler Stream (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersStreamTypeLevelSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that ambiguous handler Topic" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersTopiSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that ambiguous handler Topic (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[AmbiguousHandlersTopicTypeLevelSubscriptionInAction]).failIfInvalid
      }.getMessage should include(
        "Ambiguous handlers for java.lang.Integer, methods: [methodOne, methodTwo] consume the same type.")
    }

    "validates that source is missing for topic publication" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingSourceForTopicPublishing]).failIfInvalid
      }.getMessage should include(
        "You must select a source for @Produce.ToTopic. Annotate this methods with one a @Consume annotation.")
    }

    "validates that topic is missing for VE subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForVESubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from ValueEntity \"ve-counter\". Or remove it from all methods.")
    }

    "validates that topic is missing for ES subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForESSubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from EventSourcedEntity \"employee\". Or remove it from all methods.")
    }

    "validates that topic is missing for ES subscription (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForTypeLevelESSubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from EventSourcedEntity \"employee\". Or remove it from all methods.")
    }

    "validates that topic is missing for Topic subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForTopicSubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from Topic \"source\". Or remove it from all methods.")
    }

    "validates that topic is missing for Topic subscription (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForTopicTypeLevelSubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from Topic \"source\". Or remove it from all methods.")
    }

    "validates that topic is missing for Stream subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingTopicForStreamSubscription]).failIfInvalid
      }.getMessage should include(
        "Add @Produce.ToTopic annotation to all subscription methods from Stream \"source\". Or remove it from all methods.")
    }

    "validates that topic names are the same for VE subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForVESubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source ValueEntity \"ve-counter\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates that topic names are the same for ES subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForESSubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source EventSourcedEntity \"employee\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates that topic names are the same for ES subscription (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForESTypeLevelSubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source EventSourcedEntity \"employee\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates that topic names are the same for Topic subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForTopicSubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source Topic \"source\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates that topic names are the same for Topic subscription (type level)" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForTopicTypeLevelSubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source Topic \"source\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates that topic names are the same for Stream subscription" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[DifferentTopicForStreamSubscription]).failIfInvalid
      }.getMessage should include(
        "All @Produce.ToTopic annotation for the same subscription source Stream \"source\" should point to the same topic name. Create a separate Action if you want to split messages to different topics from the same source.")
    }

    "validates if there are missing event handlers for event sourced Entity Subscription at type level" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingHandlersWhenSubscribeToEventSourcedEntityAction]).failIfInvalid
      }.getMessage shouldBe
      "On 'akka.platform.spring.testmodels.subscriptions.PubSubTestModels$MissingHandlersWhenSubscribeToEventSourcedEntityAction': missing an event handler for 'akka.platform.spring.testmodels.eventsourcedentity.EmployeeEvent$EmployeeEmailUpdated'."
    }

    "validates if there are missing event handlers for event sourced Entity Subscription at method level" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[MissingHandlersWhenSubscribeToEventSourcedOnMethodLevelEntityAction]).failIfInvalid
      }.getMessage shouldBe
      "On 'akka.platform.spring.testmodels.subscriptions.PubSubTestModels$MissingHandlersWhenSubscribeToEventSourcedOnMethodLevelEntityAction': missing an event handler for 'akka.platform.spring.testmodels.eventsourcedentity.EmployeeEvent$EmployeeEmailUpdated'."
    }

    "validates it is forbidden Topic Subscription at annotation type level and method level at the same time" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[InvalidSubscribeToTopicAction]).failIfInvalid
      }.getMessage should include("You cannot use @Consume.FormTopic annotation in both methods and class.")
    }

    "validates consumer group must be the same per topic" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[InvalidConsumerGroupsWhenSubscribingToTopicAction]).failIfInvalid
      }.getMessage should include(
        "All subscription methods to topic [topicXYZ] must have the same consumer group, but found different consumer groups [cg, cg2].")
    }

    "generate mapping for an Action with a VE subscription and publication to a topic" in {
      assertDescriptor[VEWithPublishToTopicAction] { desc =>
        val methodOne = desc.commandHandlers("MessageOne")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "MessageOne").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"

        // should have a default extractor for any payload
        val javaMethodOne = methodOne.methodInvokers.values.head
        javaMethodOne.parameterExtractors.length shouldBe 1

        val methodTwo = desc.commandHandlers("MessageTwo")
        methodTwo.requestMessageDescriptor.getFullName shouldBe Empty.javaDescriptor.getFullName

        val eventDestinationTwo = findKalixMethodOptions(desc, "MessageTwo").getEventing.getOut
        eventDestinationTwo.getTopic shouldBe "foobar"

        // delete handler with 0 params
        val javaMethodTwo = methodTwo.methodInvokers.values.head
        javaMethodTwo.parameterExtractors.length shouldBe 0
      }
    }

    "generate mapping for an Action with raw bytes publication to a topic" in {
      assertDescriptor[PublishBytesToTopicAction] { desc =>
        val methodOne = desc.commandHandlers("Produce")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val methodDescriptor = findMethodByName(desc, "Produce")
        methodDescriptor.getInputType.getFullName shouldBe JavaPbAny.getDescriptor.getFullName
        methodDescriptor.getOutputType.getFullName shouldBe BytesValue.getDescriptor.getFullName
      }
    }

    "generate mapping for an Action subscribing to raw bytes from a topic" in {
      assertDescriptor[SubscribeToBytesFromTopicAction] { desc =>
        val methodOne = desc.commandHandlers("Consume")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        methodOne.methodInvokers.get("type.kalix.io/bytes").isDefined shouldBe true

        val methodDescriptor = findMethodByName(desc, "Consume")
        methodDescriptor.getInputType.getFullName shouldBe BytesValue.getDescriptor.getFullName
        methodDescriptor.getOutputType.getFullName shouldBe JavaPbAny.getDescriptor.getFullName
      }
    }

    "generate mapping for an Action with a ES subscription and publication to a topic" in {
      assertDescriptor[ESWithPublishToTopicAction] { desc =>
        desc.commandHandlers should have size 1

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnESEmployee")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnESEmployee").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"
      }
    }

    "generate mapping for an Action with a ES type level subscription and publication to a topic" in {
      assertDescriptor[TypeLevelESWithPublishToTopicAction] { desc =>
        desc.commandHandlers should have size 1

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnESEmployee")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnESEmployee").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"
      }
    }

    "generate mapping for an Action with a Topic subscription and publication to a topic" in {
      assertDescriptor[TypeLevelTopicSubscriptionWithPublishToTopicAction] { desc =>
        desc.commandHandlers should have size 1

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnTopicSource")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnTopicSource").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"
      }
    }

    "generate mapping for an Action with a Topic type level subscription and publication to a topic" in {
      assertDescriptor[TypeLevelTopicSubscriptionWithPublishToTopicAction] { desc =>
        desc.commandHandlers should have size 1

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnTopicSource")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnTopicSource").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"
      }
    }

    "generate mapping for an Action with a Stream subscription and publication to a topic" in {
      assertDescriptor[StreamSubscriptionWithPublishToTopicAction] { desc =>
        desc.commandHandlers should have size 1

        val methodOne = desc.commandHandlers("KalixSyntheticMethodOnStreamSource")
        methodOne.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        val eventDestinationOne = findKalixMethodOptions(desc, "KalixSyntheticMethodOnStreamSource").getEventing.getOut
        eventDestinationOne.getTopic shouldBe "foobar"
      }
    }

    "generate ACL annotations at service level" in {
      assertDescriptor[ActionWithServiceLevelAcl] { desc =>
        val extension = desc.serviceDescriptor.getOptions.getExtension(kalix.Annotations.service)
        val service = extension.getAcl.getAllow(0).getService
        service shouldBe "test"
      }
    }

    "generate ACL annotations at method level" in {
      assertDescriptor[ActionWithMethodLevelAcl] { desc =>
        val extension = findKalixMethodOptions(desc, "MessageOne")
        val service = extension.getAcl.getAllow(0).getService
        service shouldBe "test"
      }
    }

    "fail if it's subscription method exposed with ACL" in {
      intercept[InvalidComponentException] {
        Validations.validate(classOf[ActionWithMethodLevelAclAndSubscription]).failIfInvalid
      }.getMessage should include(
        "Methods annotated with Kalix @Consume annotations are for internal use only and cannot be annotated with ACL annotations.")
    }

    "generate mappings for service to service publishing " in {
      assertDescriptor[EventStreamPublishingAction] { desc =>
        val serviceOptions = findKalixServiceOptions(desc)

        val eventingOut = serviceOptions.getEventing.getOut
        eventingOut.getDirect.getEventStreamId shouldBe "employee_events"

        val methodDescriptor = findMethodByName(desc, "KalixSyntheticMethodOnESEmployee")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false

        val eventingIn = serviceOptions.getEventing.getIn
        val entityType = eventingIn.getEventSourcedEntity
        entityType shouldBe "employee"

        val onUpdateMethod = desc.commandHandlers("KalixSyntheticMethodOnESEmployee")
        onUpdateMethod.requestMessageDescriptor.getFullName shouldBe JavaPbAny.getDescriptor.getFullName

        onUpdateMethod.methodInvokers.view.mapValues(_.method.getName).toMap should
        contain only ("json.kalix.io/created" -> "transform", "json.kalix.io/old-created" -> "transform", "json.kalix.io/emailUpdated" -> "transform")
      }
    }

    "generate mappings for service to service subscription " in {
      assertDescriptor[EventStreamSubscriptionAction] { desc =>
        val serviceOptions = findKalixServiceOptions(desc)

        val eventingIn = serviceOptions.getEventing.getIn
        val eventingInDirect = eventingIn.getDirect
        eventingInDirect.getService shouldBe "employee_service"
        eventingInDirect.getEventStreamId shouldBe "employee_events"

        // we don't set the property so the proxy won't ignore. Ignore is only internal to the SDK
        eventingIn.getIgnore shouldBe false
        eventingIn.getIgnoreUnknown shouldBe false

        val methodDescriptor = findMethodByName(desc, "KalixSyntheticMethodOnStreamEmployeeevents")
        methodDescriptor.isServerStreaming shouldBe false
        methodDescriptor.isClientStreaming shouldBe false
      }
    }
  }

}