/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.workflow;

import akka.javasdk.annotations.ComponentId;

import java.util.concurrent.CompletableFuture;

@ComponentId("workflow")
public class TestWorkflowSerialization extends Workflow<String> {

  @Override
  public WorkflowDef<String> definition() {
    var testStep = step("test")
        .asyncCall(() -> CompletableFuture.<Result>completedFuture(new Result.Succeed()))
        .andThen(Result.class, result -> effects().updateState("success").end());

    return workflow().addStep(testStep);
  }

  public Effect<String> start() {
    return effects()
        .updateState("empty")
        .transitionTo("test")
        .thenReply("ok");
  }

  public Effect<String> get() {
    return effects().reply(currentState());
  }
}