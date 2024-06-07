/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package kalix.javasdk.impl.client

import java.lang.reflect.Method
import kalix.javasdk.annotations.Query
import kalix.javasdk.action.Action
import kalix.javasdk.eventsourcedentity.EventSourcedEntity
import kalix.javasdk.valueentity.ValueEntity
import kalix.javasdk.workflow.Workflow

object ViewCallValidator {

  def validate(method: Method): Unit = {
    val declaringClass = method.getDeclaringClass
    if (classOf[Action].isAssignableFrom(declaringClass)
      || classOf[ValueEntity[_]].isAssignableFrom(declaringClass)
      || classOf[EventSourcedEntity[_, _]].isAssignableFrom(declaringClass)
      || classOf[Workflow[_]].isAssignableFrom(declaringClass)) {
      throw new IllegalStateException(
        "Use dedicated builder for calling " + declaringClass.getSuperclass.getSimpleName
        + " component method " + declaringClass.getSimpleName + "::" + method.getName + ". This builder is meant for View component calls.")
    }

    if (!method.getAnnotations.toSeq.exists(annotation =>
        classOf[Query].isAssignableFrom(annotation.annotationType()))) {
      throw new IllegalStateException(
        s"A View query method [${method.getName}] should be annotated with @Query annotation.")
    }
  }
}
