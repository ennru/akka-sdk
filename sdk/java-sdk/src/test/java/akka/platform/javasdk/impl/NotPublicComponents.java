/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.impl;

import akka.platform.javasdk.action.Action;
import akka.platform.javasdk.annotations.ActionId;
import akka.platform.javasdk.annotations.Query;
import akka.platform.javasdk.annotations.Consume;
import akka.platform.javasdk.annotations.Table;
import akka.platform.javasdk.annotations.TypeId;
import akka.platform.javasdk.annotations.ViewId;
import akka.platform.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.platform.javasdk.valueentity.ValueEntity;
import akka.platform.javasdk.view.View;
import akka.platform.javasdk.workflow.Workflow;
import akka.platform.spring.testmodels.Message;
import akka.platform.spring.testmodels.valueentity.User;
import akka.platform.spring.testmodels.valueentity.UserEntity;
import akka.platform.spring.testmodels.workflow.StartWorkflow;
import akka.platform.spring.testmodels.workflow.WorkflowState;

// below components are not public and thus need to be in the same package as the corresponding test
public class NotPublicComponents {
  @ActionId("not-public")
  static class NotPublicAction extends Action {
    public Action.Effect<Message> message() {
      return effects().ignore();
    }
  }

  @TypeId("counter")
  static class NotPublicEventSourced extends EventSourcedEntity<Integer, NotPublicEventSourced.Event> {

    public sealed interface Event {
      public record Created()implements Event {};
    }

    public Integer test() {
      return 0;
    }

    @Override
    public Integer applyEvent(Event event) {
      return 0;
    }
  }

  @TypeId("user")
  static class NotPublicValueEntity extends ValueEntity<User> {
    public ValueEntity.Effect<String> ok() {
      return effects().reply("ok");
    }
  }

  @ViewId("users_view")
  @Table(value = "users_view")
  @Consume.FromValueEntity(UserEntity.class)
  static class NotPublicView extends View<User> {

    public record QueryParameters(String email) {}

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(QueryParameters email) {
      return null;
    }
  }

  @TypeId("transfer-workflow")
  static class NotPublicWorkflow extends Workflow<WorkflowState> {
    @Override
    public WorkflowDef<WorkflowState> definition() {
      return null;
    }

    public Effect<String> startTransfer(StartWorkflow startWorkflow) {
      return null;
    }
  }
}

