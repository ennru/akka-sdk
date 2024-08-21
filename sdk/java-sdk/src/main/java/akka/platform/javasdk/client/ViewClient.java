/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.client;

import akka.annotation.DoNotInherit;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.platform.javasdk.view.View;

/** Not for user extension */
@DoNotInherit
public interface ViewClient {

  /**
   * Pass in a View query method reference, e.g. <code>UserByCity::find</code> If no result is
   * found, the result of the request will be a {@link NoEntryFoundException}
   */
  <T, R> ComponentInvokeOnlyMethodRef<R> method(Function<T, View.QueryEffect<R>> methodRef);

  /**
   * Pass in a View query method reference, e.g. <code>UserByCity::find</code
   *
   * If no result is found, the result of the request will be a {@link NoEntryFoundException}
   */
  <T, A1, R> ComponentInvokeOnlyMethodRef1<A1, R> method(Function2<T, A1, View.QueryEffect<R>> methodRef);
}
