/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.impl

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import scala.reflect.ClassTag
import akka.platform.javasdk.action.Action
import akka.platform.javasdk.annotations.ComponentId
import akka.platform.javasdk.annotations.Consume.FromKeyValueEntity
import akka.platform.javasdk.annotations.Produce.ServiceStream
import akka.platform.javasdk.annotations.Query
import akka.platform.javasdk.annotations.Table
import akka.platform.javasdk.consumer.Consumer
import akka.platform.javasdk.eventsourcedentity.EventSourcedEntity
import akka.platform.javasdk.impl.ComponentDescriptorFactory.eventSourcedEntitySubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findEventSourcedEntityClass
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findEventSourcedEntityType
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findPublicationTopicName
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findSubscriptionConsumerGroup
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findSubscriptionSourceName
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findSubscriptionTopicName
import akka.platform.javasdk.impl.ComponentDescriptorFactory.findValueEntityType
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasAcl
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasActionOutput
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasConsumerOutput
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasEventSourcedEntitySubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasHandleDeletes
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasStreamSubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasSubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasTopicPublication
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasTopicSubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasViewUpdateEffectOutput
import akka.platform.javasdk.impl.ComponentDescriptorFactory.hasValueEntitySubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.streamSubscription
import akka.platform.javasdk.impl.ComponentDescriptorFactory.topicSubscription
import akka.platform.javasdk.impl.reflection.Reflect
import akka.platform.javasdk.impl.reflection.Reflect.Syntax._
import akka.platform.javasdk.keyvalueentity.KeyValueEntity
import akka.platform.javasdk.view.View

object Validations {

  import Reflect.methodOrdering

  object Validation {

    def apply(messages: Array[String]): Validation = Validation(messages.toIndexedSeq)

    def apply(messages: Seq[String]): Validation =
      if (messages.isEmpty) Valid
      else Invalid(messages)

    def apply(message: String): Validation = Invalid(Seq(message))
  }

  sealed trait Validation {
    def isValid: Boolean

    final def isInvalid: Boolean = !isInvalid
    def ++(validation: Validation): Validation

    def failIfInvalid: Validation
  }

  case object Valid extends Validation {
    override def isValid: Boolean = true
    override def ++(validation: Validation): Validation = validation

    override def failIfInvalid: Validation = this
  }

  object Invalid {
    def apply(message: String): Invalid =
      Invalid(Seq(message))
  }

  case class Invalid(messages: Seq[String]) extends Validation {
    override def isValid: Boolean = false

    override def ++(validation: Validation): Validation =
      validation match {
        case Valid      => this
        case i: Invalid => Invalid(this.messages ++ i.messages)
      }

    override def failIfInvalid: Validation =
      throw InvalidComponentException(messages.mkString(", "))
  }

  private def when(cond: Boolean)(block: => Validation): Validation =
    if (cond) block else Valid

  private def when[T: ClassTag](component: Class[_])(block: => Validation): Validation =
    if (assignable[T](component)) block else Valid

  private def assignable[T: ClassTag](component: Class[_]): Boolean =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]].isAssignableFrom(component)

  private def commonSubscriptionValidation(
      component: Class[_],
      updateMethodPredicate: Method => Boolean): Validation = {
    typeLevelSubscriptionValidation(component) ++
    eventSourcedEntitySubscriptionValidations(component) ++
    missingEventHandlerValidations(component, updateMethodPredicate) ++
    ambiguousHandlerValidations(component, updateMethodPredicate) ++
    valueEntitySubscriptionValidations(component, updateMethodPredicate) ++
    topicSubscriptionValidations(component) ++
    topicPublicationValidations(component, updateMethodPredicate) ++
    publishStreamIdMustBeFilled(component) ++
    noSubscriptionMethodWithAcl(component) ++
    subscriptionMethodMustHaveOneParameter(component)
  }

  def validate(component: Class[_]): Validation =
    componentMustBePublic(component) ++
    validateAction(component) ++
    validateConsumer(component) ++
    validateView(component) ++
    validateEventSourcedEntity(component) ++
    validateValueEntity(component)

  private def validateEventSourcedEntity(component: Class[_]) =
    when[EventSourcedEntity[_, _]](component) {
      eventSourcedEntityEventMustBeSealed(component) ++
      eventSourcedCommandHandlersMustBeUnique(component) ++
      mustHaveNonEmptyComponentId(component)
    }

  private def eventSourcedEntityEventMustBeSealed(component: Class[_]): Validation = {
    val eventType =
      component.getGenericSuperclass
        .asInstanceOf[ParameterizedType]
        .getActualTypeArguments()(1)
        .asInstanceOf[Class[_]]

    when(!eventType.isSealed) {
      Invalid(
        s"The event type of an EventSourcedEntity is required to be a sealed interface. Event '${eventType.getName}' in '${component.getName}' is not sealed.")
    }
  }

  private def eventSourcedCommandHandlersMustBeUnique(component: Class[_]): Validation = {
    val commandHandlers = component.getMethods
      .filter(_.getReturnType == classOf[EventSourcedEntity.Effect[_]])
    commandHandlersMustBeUnique(component, commandHandlers)
  }

  def validateValueEntity(component: Class[_]): Validation = when[KeyValueEntity[_]](component) {
    valueEntityCommandHandlersMustBeUnique(component) ++
    mustHaveNonEmptyComponentId(component)
  }

  def valueEntityCommandHandlersMustBeUnique(component: Class[_]): Validation = {
    val commandHandlers = component.getMethods
      .filter(_.getReturnType == classOf[KeyValueEntity.Effect[_]])
    commandHandlersMustBeUnique(component, commandHandlers)
  }

  def commandHandlersMustBeUnique(component: Class[_], commandHandlers: Array[Method]): Validation = {
    val nonUnique = commandHandlers
      .groupBy(_.getName)
      .filter(_._2.length > 1)

    when(nonUnique.nonEmpty) {
      val nonUniqueWarnings =
        nonUnique
          .map { case (name, methods) => s"${methods.length} command handler methods named '$name'" }
          .mkString(", ")
      Invalid(
        errorMessage(
          component,
          s"${component.getSimpleName} has $nonUniqueWarnings. Command handlers must have unique names."))
    }
  }

  private def componentMustBePublic(component: Class[_]): Validation = {
    if (component.isPublic) {
      Valid
    } else {
      Invalid(
        errorMessage(
          component,
          s"${component.getSimpleName} is not marked with `public` modifier. Components must be public."))
    }
  }

  private def validateAction(component: Class[_]): Validation = {
    when[Action](component) {
      commonSubscriptionValidation(component, hasActionOutput) ++
      actionValidation(component) ++
      mustHaveNonEmptyComponentId(component)
    }
  }

  private def validateConsumer(component: Class[_]): Validation = {
    when[Consumer](component) {
      commonSubscriptionValidation(component, hasConsumerOutput) ++
      actionValidation(component) ++
      mustHaveNonEmptyComponentId(component)
    }
  }

  private def actionValidation(component: Class[_]): Validation = {
    // Nothing here right now
    Valid
  }

  private def validateView(component: Class[_]): Validation =
    when[View](component) {
      val tableUpdaters = component.getDeclaredClasses.filter(Reflect.isViewTableUpdater).toSeq

      mustHaveNonEmptyComponentId(component) ++
      viewMustNotHaveTableAnnotation(component) ++
      viewMustHaveAtLeastOneViewTableUpdater(component) ++
      viewMustHaveAtLeastOneQueryMethod(component) ++
      viewQueriesMustReturnEffect(component) ++
      viewMultipleTableUpdatersMustHaveTableAnnotations(tableUpdaters) ++
      tableUpdaters
        .map(updaterClass =>
          validateVewTableUpdater(updaterClass) ++
          viewTableAnnotationMustNotBeEmptyString(updaterClass) ++
          viewMustHaveMethodLevelSubscriptionWhenTransformingUpdates(updaterClass))
        .foldLeft(Valid: Validation)(_ ++ _)
      // FIXME query annotated return type should be effect
    }

  private def viewMustHaveAtLeastOneViewTableUpdater(component: Class[_]) =
    when(component.getDeclaredClasses.count(Reflect.isViewTableUpdater) < 1) {
      Validation(errorMessage(component, "A view must contain at least one public static TableUpdater subclass."))
    }

  private def validateVewTableUpdater(tableUpdater: Class[_]): Validation =
    commonSubscriptionValidation(tableUpdater, hasViewUpdateEffectOutput)

  private def viewTableAnnotationMustNotBeEmptyString(tableUpdater: Class[_]): Validation = {
    val annotation = tableUpdater.getAnnotation(classOf[Table])
    when(annotation != null && annotation.value().trim.isEmpty) {
      Validation(errorMessage(tableUpdater, "@Table name is empty, must be a non-empty string."))
    }
  }

  private def viewQueriesMustReturnEffect(component: Class[_]): Validation = {
    val queriesWithWrongReturnType = component.getMethods.toIndexedSeq.filter(m =>
      m.getAnnotation(classOf[Query]) != null && m.getReturnType != classOf[View.QueryEffect[_]])
    queriesWithWrongReturnType.foldLeft(Valid: Validation) { (validation, methodWithWrongReturnType) =>
      validation ++ Validation(
        errorMessage(methodWithWrongReturnType, "Query methods must return View.QueryEffect<RowType>."))
    }
  }

  private def viewMultipleTableUpdatersMustHaveTableAnnotations(tableUpdaters: Seq[Class[_]]): Validation =
    if (tableUpdaters.size > 1) {
      tableUpdaters.find(_.getAnnotation(classOf[Table]) == null) match {
        case Some(clazz) =>
          Validation(errorMessage(clazz, "When there are multiple table updater, each must be annotated with @Table."))
        case None => Valid
      }
    } else Valid

  private def errorMessage(element: AnnotatedElement, message: String): String = {
    val elementStr =
      element match {
        case clz: Class[_] => clz.getName
        case meth: Method  => s"${meth.getDeclaringClass.getName}#${meth.getName}"
        case any           => any.toString
      }
    s"On '$elementStr': $message"
  }

  def typeLevelSubscriptionValidation(component: Class[_]): Validation = {
    val typeLevelSubs = List(
      hasValueEntitySubscription(component),
      hasEventSourcedEntitySubscription(component),
      hasStreamSubscription(component),
      hasTopicSubscription(component))

    when(typeLevelSubs.count(identity) > 1) {
      Validation(errorMessage(component, "Only one subscription type is allowed on a type level."))
    }
  }

  private def eventSourcedEntitySubscriptionValidations(component: Class[_]): Validation = {
    val methods = component.getMethods.toIndexedSeq
    when(
      hasEventSourcedEntitySubscription(component) &&
      methods.exists(hasEventSourcedEntitySubscription)) {
      // collect offending methods
      val messages = methods.filter(hasEventSourcedEntitySubscription).map { method =>
        errorMessage(
          method,
          "You cannot use @Consume.FromEventSourcedEntity annotation in both methods and class. You can do either one or the other.")
      }
      Validation(messages)
    }
  }

  private def getEventType(esEntity: Class[_]): Class[_] = {
    val genericTypeArguments = esEntity.getGenericSuperclass
      .asInstanceOf[ParameterizedType]
      .getActualTypeArguments
    genericTypeArguments(1).asInstanceOf[Class[_]]
  }

  private def ambiguousHandlerValidations(component: Class[_], updateMethodPredicate: Method => Boolean): Validation = {

    val methods = component.getMethods.toIndexedSeq

    if (hasSubscription(component)) {
      val effectMethods = methods
        .filter(updateMethodPredicate)
      ambiguousHandlersErrors(effectMethods, component)
    } else {
      val effectOutputMethodsGrouped = methods
        .filter(hasSubscription)
        .filter(updateMethodPredicate)
        .groupBy(findSubscriptionSourceName)

      effectOutputMethodsGrouped
        .map { case (_, methods) =>
          ambiguousHandlersErrors(methods, component)
        }
        .fold(Valid)(_ ++ _)
    }

  }

  private def ambiguousHandlersErrors(handlers: Seq[Method], component: Class[_]): Validation = {
    val ambiguousHandlers = handlers
      .groupBy(_.getParameterTypes.lastOption)
      .filter(_._2.size > 1)
      .map {
        case (Some(inputType), methods) =>
          Validation(errorMessage(
            component,
            s"Ambiguous handlers for ${inputType.getCanonicalName}, methods: [${methods.sorted.map(_.getName).mkString(", ")}] consume the same type."))
        case (None, methods) => //only delete handlers
          Validation(
            errorMessage(component, s"Ambiguous delete handlers: [${methods.sorted.map(_.getName).mkString(", ")}]."))
      }

    val sealedHandler = handlers.find(_.getParameterTypes.lastOption.exists(_.isSealed))
    val sealedHandlerMixedUsage = if (sealedHandler.nonEmpty && handlers.size > 1) {
      val unexpectedHandlerNames = handlers.filterNot(m => m == sealedHandler.get).map(_.getName)
      Validation(
        errorMessage(
          component,
          s"Event handler accepting a sealed interface [${sealedHandler.get.getName}] cannot be mixed with handlers for specific events. Please remove following handlers: [${unexpectedHandlerNames
            .mkString(", ")}]."))
    } else {
      Valid
    }

    ambiguousHandlers.fold(sealedHandlerMixedUsage)(_ ++ _)
  }

  private def missingEventHandlerValidations(
      component: Class[_],
      updateMethodPredicate: Method => Boolean): Validation = {
    val methods = component.getMethods.toIndexedSeq

    eventSourcedEntitySubscription(component) match {
      case Some(classLevel) =>
        val eventType = getEventType(classLevel.value())
        if (!classLevel.ignoreUnknown() && eventType.isSealed) {
          val effectMethodsInputParams: Seq[Class[_]] = methods
            .filter(updateMethodPredicate)
            .map(_.getParameterTypes.last) //last because it could be a view update methods with 2 params
          missingEventHandler(effectMethodsInputParams, eventType, component)
        } else {
          Valid
        }
      case None =>
        //method level
        val effectOutputMethodsGrouped = methods
          .filter(hasEventSourcedEntitySubscription)
          .filter(updateMethodPredicate)
          .groupBy(findEventSourcedEntityClass)

        effectOutputMethodsGrouped
          .map { case (entityClass, methods) =>
            val eventType = getEventType(entityClass)
            if (eventType.isSealed) {
              missingEventHandler(methods.map(_.getParameterTypes.last), eventType, component)
            } else {
              Valid
            }
          }
          .fold(Valid)(_ ++ _)
    }
  }

  private def missingEventHandler(
      inputEventParams: Seq[Class[_]],
      eventType: Class[_],
      component: Class[_]): Validation = {
    if (inputEventParams.exists(param => param.isSealed && param == eventType)) {
      //single sealed interface handler
      Valid
    } else {
      if (eventType.isSealed) {
        //checking possible only for sealed interfaces
        Validation(
          eventType.getPermittedSubclasses
            .filterNot(inputEventParams.contains)
            .map(clazz => errorMessage(component, s"missing an event handler for '${clazz.getName}'."))
            .toList)
      } else {
        Valid
      }
    }
  }

  private def topicSubscriptionValidations(component: Class[_]): Validation = {
    val methods = component.getMethods.toIndexedSeq
    val noMixedLevelSubs = when(hasTopicSubscription(component) && methods.exists(hasTopicSubscription)) {
      // collect offending methods
      val messages = methods.filter(hasTopicSubscription).map { method =>
        errorMessage(
          method,
          "You cannot use @Consume.FormTopic annotation in both methods and class. You can do either one or the other.")
      }
      Validation(messages)
    }

    val theSameConsumerGroupPerTopic = when(methods.exists(hasTopicSubscription)) {
      methods
        .filter(hasTopicSubscription)
        .sorted
        .groupBy(findSubscriptionTopicName)
        .map { case (topicName, methods) =>
          val consumerGroups = methods.map(findSubscriptionConsumerGroup).distinct.sorted
          when(consumerGroups.size > 1) {
            Validation(errorMessage(
              component,
              s"All subscription methods to topic [$topicName] must have the same consumer group, but found different consumer groups [${consumerGroups
                .mkString(", ")}]."))
          }
        }
        .fold(Valid)(_ ++ _)
    }

    noMixedLevelSubs ++ theSameConsumerGroupPerTopic
  }

  private def missingSourceForTopicPublication(component: Class[_]): Validation = {
    val methods = component.getMethods.toSeq
    if (hasSubscription(component)) {
      Valid
    } else {
      val messages = methods
        .filter(hasTopicPublication)
        .filterNot(method => hasSubscription(method))
        .map { method =>
          errorMessage(
            method,
            "You must select a source for @Produce.ToTopic. Annotate this methods with one a @Consume annotation.")
        }
      Validation(messages)
    }
  }

  private def topicPublicationForSourceValidation(
      sourceName: String,
      component: Class[_],
      methodsGroupedBySource: Map[String, Seq[Method]]): Validation = {
    methodsGroupedBySource
      .map { case (entityType, methods) =>
        val topicNames = methods
          .filter(hasTopicPublication)
          .map(findPublicationTopicName)

        if (topicNames.nonEmpty && topicNames.length != methods.size) {
          Validation(errorMessage(
            component,
            s"Add @Produce.ToTopic annotation to all subscription methods from $sourceName \"$entityType\". Or remove it from all methods."))
        } else if (topicNames.toSet.size > 1) {
          Validation(
            errorMessage(
              component,
              s"All @Produce.ToTopic annotation for the same subscription source $sourceName \"$entityType\" should point to the same topic name. " +
              s"Create a separate Action if you want to split messages to different topics from the same source."))
        } else {
          Valid
        }
      }
      .fold(Valid)(_ ++ _)
  }

  private def topicPublicationValidations(component: Class[_], updateMethodPredicate: Method => Boolean): Validation = {
    val methods = component.getMethods.toSeq

    //VE type level subscription is not checked since we expecting only a single method in this case
    val veSubscriptions: Map[String, Seq[Method]] = methods
      .filter(hasValueEntitySubscription)
      .groupBy(findValueEntityType)

    val esSubscriptions: Map[String, Seq[Method]] = eventSourcedEntitySubscription(component) match {
      case Some(esEntity) =>
        Map(
          ComponentDescriptorFactory.readComponentIdIdValue(esEntity.value()) -> methods.filter(updateMethodPredicate))
      case None =>
        methods
          .filter(hasEventSourcedEntitySubscription)
          .groupBy(findEventSourcedEntityType)
    }

    val topicSubscriptions: Map[String, Seq[Method]] = topicSubscription(component) match {
      case Some(topic) => Map(topic.value() -> methods.filter(updateMethodPredicate))
      case None =>
        methods
          .filter(hasTopicSubscription)
          .groupBy(findSubscriptionTopicName)
    }

    val streamSubscriptions: Map[String, Seq[Method]] = streamSubscription(component) match {
      case Some(stream) => Map(stream.id() -> methods.filter(updateMethodPredicate))
      case None         => Map.empty //only type level
    }

    missingSourceForTopicPublication(component) ++
    topicPublicationForSourceValidation("ValueEntity", component, veSubscriptions) ++
    topicPublicationForSourceValidation("EventSourcedEntity", component, esSubscriptions) ++
    topicPublicationForSourceValidation("Topic", component, topicSubscriptions) ++
    topicPublicationForSourceValidation("Stream", component, streamSubscriptions)
  }

  private def publishStreamIdMustBeFilled(component: Class[_]): Validation = {
    Option(component.getAnnotation(classOf[ServiceStream]))
      .map { ann =>
        when(ann.id().trim.isEmpty) {
          Validation(Seq("@Produce.ServiceStream id can not be an empty string"))
        }
      }
      .getOrElse(Valid)
  }

  private def noSubscriptionMethodWithAcl(component: Class[_]): Validation = {

    val hasSubscriptionAndAcl = (method: Method) => hasAcl(method) && hasSubscription(method)

    val messages =
      component.getMethods.toIndexedSeq.filter(hasSubscriptionAndAcl).map { method =>
        errorMessage(
          method,
          "Methods annotated with Kalix @Consume annotations are for internal use only and cannot be annotated with ACL annotations.")
      }

    Validation(messages)
  }

  private def mustHaveNonEmptyComponentId(component: Class[_]): Validation = {
    val ann = component.getAnnotation(classOf[ComponentId])
    if (ann != null) {
      val componentId: String = ann.value()
      if (componentId == null || componentId.trim.isEmpty) {
        Invalid(errorMessage(component, "@ComponentId name is empty, must be a non-empty string."))
      } else Valid
    } else {
      //missing annotation means that the component is disabled
      Valid
    }
  }

  private def viewMustNotHaveTableAnnotation(component: Class[_]): Validation = {
    val ann = component.getAnnotation(classOf[Table])
    when(ann != null) {
      Invalid(errorMessage(component, "A View itself should not be annotated with @Table."))
    }
  }

  private def viewMustHaveMethodLevelSubscriptionWhenTransformingUpdates(tableUpdater: Class[_]): Validation = {
    if (hasValueEntitySubscription(tableUpdater)) {
      val tableType: Class[_] = tableTypeOf(tableUpdater)
      val valueEntityClass: Class[_] =
        tableUpdater.getAnnotation(classOf[FromKeyValueEntity]).value().asInstanceOf[Class[_]]
      val entityStateClass = valueEntityStateClassOf(valueEntityClass)

      when(entityStateClass != tableType) {
        val message =
          s"You are using a type level annotation on View TableUpdater [$tableUpdater] and that requires updater row type [${tableType.getName}] " +
          s"to match the ValueEntity type [${entityStateClass.getName}]. " +
          s"If your intention is to transform the type, you should instead add a method like " +
          s"`UpdateEffect<${tableType.getName}> onChange(${entityStateClass.getName} state)`" +
          " and move the @Consume.FromKeyValueEntity to it."

        Validation(Seq(errorMessage(tableUpdater, message)))
      }
    } else {
      Valid
    }
  }

  private def viewMustHaveAtLeastOneQueryMethod(component: Class[_]): Validation = {
    val hasAtLeastOneQuery =
      component.getMethods.exists(method => method.hasAnnotation[Query])
    if (!hasAtLeastOneQuery)
      Invalid(
        errorMessage(
          component,
          "No valid query method found. Views should have at least one method annotated with @Query."))
    else Valid
  }

  private def subscriptionMethodMustHaveOneParameter(component: Class[_]): Validation = {
    val offendingMethods = component.getMethods
      .filter(hasValueEntitySubscription)
      .filterNot(hasHandleDeletes)
      .filterNot(_.getParameterTypes.length == 1)

    val messages =
      offendingMethods.map { method =>
        errorMessage(
          method,
          "Subscription method must have exactly one parameter, unless it's marked as handleDeletes.")
      }

    Validation(messages)
  }

  private def valueEntitySubscriptionValidations(
      component: Class[_],
      updateMethodPredicate: Method => Boolean): Validation = {

    val subscriptionMethods = component.getMethods.toIndexedSeq.filter(hasValueEntitySubscription).sorted
    val updatedMethods = if (hasValueEntitySubscription(component)) {
      component.getMethods.toIndexedSeq.filter(updateMethodPredicate).sorted
    } else {
      subscriptionMethods.filterNot(hasHandleDeletes).filter(updateMethodPredicate)
    }

    val (handleDeleteMethods, handleDeleteMethodsWithParam) =
      subscriptionMethods.filter(hasHandleDeletes).partition(_.getParameterTypes.isEmpty)

    val noMixedLevelValueEntitySubscription =
      when(hasValueEntitySubscription(component) && subscriptionMethods.nonEmpty) {
        // collect offending methods
        val messages = subscriptionMethods.map { method =>
          errorMessage(
            method,
            "You cannot use @Consume.FromKeyValueEntity annotation in both methods and class. You can do either one or the other.")
        }
        Validation(messages)
      }

    val handleDeletesMustHaveZeroArity = {
      val messages =
        handleDeleteMethodsWithParam.map { method =>
          val numParams = method.getParameters.length
          errorMessage(
            method,
            s"Method annotated with '@Consume.FromKeyValueEntity' and handleDeletes=true must not have parameters. Found $numParams method parameters.")
        }

      Validation(messages)
    }

    val onlyOneValueEntityUpdateIsAllowed = {
      if (updatedMethods.size >= 2) {
        val messages = errorMessage(
          component,
          s"Duplicated update methods [${updatedMethods.map(_.getName).mkString(", ")}]for KeyValueEntity subscription.")
        Validation(messages)
      } else Valid
    }

    val onlyOneHandlesDeleteIsAllowed = {
      val offendingMethods = handleDeleteMethods.filter(_.getParameterTypes.isEmpty)

      if (offendingMethods.size >= 2) {
        val messages =
          offendingMethods.map { method =>
            errorMessage(
              method,
              "Multiple methods annotated with @Consume.FromKeyValueEntity(handleDeletes=true) is not allowed.")
          }
        Validation(messages)
      } else Valid
    }

    val standaloneMethodLevelHandleDeletesIsNotAllowed = {
      if (handleDeleteMethods.nonEmpty && updatedMethods.isEmpty) {
        val messages =
          handleDeleteMethods.map { method =>
            errorMessage(method, "Method annotated with handleDeletes=true has no matching update method.")
          }
        Validation(messages)
      } else Valid
    }

    noMixedLevelValueEntitySubscription ++
    handleDeletesMustHaveZeroArity ++
    onlyOneValueEntityUpdateIsAllowed ++
    onlyOneHandlesDeleteIsAllowed ++
    standaloneMethodLevelHandleDeletesIsNotAllowed
  }

  private def tableTypeOf(component: Class[_]) = {
    component.getGenericSuperclass
      .asInstanceOf[ParameterizedType]
      .getActualTypeArguments
      .head
      .asInstanceOf[Class[_]]
  }

  private def valueEntityStateClassOf(valueEntityClass: Class[_]): Class[_] = {
    valueEntityClass.getGenericSuperclass
      .asInstanceOf[ParameterizedType]
      .getActualTypeArguments
      .head
      .asInstanceOf[Class[_]]
  }

}
