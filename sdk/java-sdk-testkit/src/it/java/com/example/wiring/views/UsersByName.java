/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package com.example.wiring.views;

import com.example.wiring.valueentities.user.User;
import com.example.wiring.valueentities.user.UserEntity;
import akka.platform.javasdk.annotations.Query;
import akka.platform.javasdk.annotations.Consume;
import akka.platform.javasdk.annotations.Table;
import akka.platform.javasdk.annotations.ViewId;
import akka.platform.javasdk.view.View;

import java.util.List;

@ViewId("users_by_name")
@Table("users_by_name")
@Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
public class UsersByName extends View<User> {

  public record QueryParameters(String name) {}
  public record UserList(List<User> users) {}

  @Query("SELECT * AS users FROM users_by_name WHERE name = :name")
  public UserList getUsers(QueryParameters params) {
    return null;
  }
}