/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.keyvalueentity;

import akka.platform.javasdk.EntityOptions;
import akka.platform.javasdk.impl.keyvalueentity.KeyValueEntityOptionsImpl;

import java.util.Set;

/** Root entity options for all value based entities. */
public interface KeyValueEntityOptions extends EntityOptions {

  @Override
  KeyValueEntityOptions withForwardHeaders(Set<String> headers);

  /**
   * Create a default entity option for a value based entity.
   *
   * @return the entity option
   */
  static KeyValueEntityOptions defaults() {
    return KeyValueEntityOptionsImpl.defaults();
  }
}