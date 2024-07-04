/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.spring.testmodels.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import akka.platform.spring.testmodels.valueentity.User;

import java.util.Collection;

public class UserCollection {

  public final Collection<User> users;

  @JsonCreator
  public UserCollection(@JsonProperty("users") Collection<User> users) {
    this.users = users;
  }
}