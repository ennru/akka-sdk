/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package com.example.wiring.keyvalueentities.user;

import com.example.wiring.Ok;
import akka.platform.javasdk.StatusCode;
import akka.platform.javasdk.action.Action;
import akka.platform.javasdk.action.ActionContext;
import akka.platform.javasdk.client.ComponentClient;

public class ValidateUserAction extends Action {

  private ActionContext ctx;
  private ComponentClient componentClient;

  public ValidateUserAction(ActionContext ctx, ComponentClient componentClient) {
    this.ctx = ctx;
    this.componentClient = componentClient;
  }

  public record CreateUser(String user, String email, String name){}
  public Action.Effect<Ok> createOrUpdateUser(CreateUser createUser) {
    if (createUser.email.isEmpty() || createUser.name.isEmpty())
      return effects().error("No field can be empty", StatusCode.ErrorCode.BAD_REQUEST);

    var reply =
      componentClient
        .forKeyValueEntity(createUser.user)
        .method(UserEntity::createUser)
        .invokeAsync(new UserEntity.CreatedUser(createUser.name, createUser.email));
    return effects().asyncReply(reply);
  }

  public record UpdateEmail(String user, String email){}
  public Action.Effect<Ok> updateEmail(UpdateEmail updateEmail) {
    if (updateEmail.email.isEmpty())
      return effects().error("No field can be empty", StatusCode.ErrorCode.BAD_REQUEST);

    var reply =
      componentClient
        .forKeyValueEntity(updateEmail.user)
        .method(UserEntity::updateEmail)
        .invokeAsync(new UserEntity.UpdateEmail(updateEmail.email));
    return effects().asyncReply(reply);
  }

  public Action.Effect<Ok> delete(String user) {
    var reply =
      componentClient
        .forKeyValueEntity(user)
        .method(UserEntity::deleteUser)
        .invokeAsync(new UserEntity.Delete());
    return effects().asyncReply(reply);
  }
}