/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package kalix.javasdk.client;

import akka.japi.function.Function;
import akka.japi.function.Function10;
import akka.japi.function.Function11;
import akka.japi.function.Function12;
import akka.japi.function.Function13;
import akka.japi.function.Function14;
import akka.japi.function.Function15;
import akka.japi.function.Function16;
import akka.japi.function.Function17;
import akka.japi.function.Function18;
import akka.japi.function.Function19;
import akka.japi.function.Function2;
import akka.japi.function.Function20;
import akka.japi.function.Function21;
import akka.japi.function.Function22;
import akka.japi.function.Function3;
import akka.japi.function.Function4;
import akka.japi.function.Function5;
import akka.japi.function.Function6;
import akka.japi.function.Function7;
import akka.japi.function.Function8;
import akka.japi.function.Function9;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.Metadata;
import kalix.javasdk.action.Action;
import kalix.spring.impl.KalixClient;

import java.util.List;
import java.util.Optional;

public class ActionClient {

  private final KalixClient kalixClient;
  private final Optional<Metadata> callMetadata;

  public ActionClient(KalixClient kalixClient, Optional<Metadata> callMetadata) {
    this.kalixClient = kalixClient;
    this.callMetadata = callMetadata;
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  // FIXME: this method should return MethodRef
  public <T, R> DeferredCall<Any, R> methodRef(Function<T, Action.Effect<R>> methodRef) {
    return ComponentMethodRef.noParams(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, R> ComponentMethodRef1<A1, R> methodRef(Function2<T, A1, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef1<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, R> ComponentMethodRef2<A1, A2, R> methodRef(Function3<T, A1, A2, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef2<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, R> ComponentMethodRef3<A1, A2, A3, R> methodRef(Function4<T, A1, A2, A3, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef3<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, R> ComponentMethodRef4<A1, A2, A3, A4, R> methodRef(Function5<T, A1, A2, A3, A4, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef4<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, R> ComponentMethodRef5<A1, A2, A3, A4, A5, R> methodRef(Function6<T, A1, A2, A3, A4, A5, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef5<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, R> ComponentMethodRef6<A1, A2, A3, A4, A5, A6, R> methodRef(Function7<T, A1, A2, A3, A4, A5, A6, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef6<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, R> ComponentMethodRef7<A1, A2, A3, A4, A5, A6, A7, R> methodRef(Function8<T, A1, A2, A3, A4, A5, A6, A7, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef7<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, R> ComponentMethodRef8<A1, A2, A3, A4, A5, A6, A7, A8, R> methodRef(Function9<T, A1, A2, A3, A4, A5, A6, A7, A8, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef8<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, R> ComponentMethodRef9<A1, A2, A3, A4, A5, A6, A7, A8, A9, R> methodRef(Function10<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef9<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R> ComponentMethodRef10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R> methodRef(Function11<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef10<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R> ComponentMethodRef11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R> methodRef(Function12<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef11<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R> ComponentMethodRef12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R> methodRef(Function13<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef12<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R> ComponentMethodRef13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R> methodRef(Function14<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef13<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R> ComponentMethodRef14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R> methodRef(Function15<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef14<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R> ComponentMethodRef15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R> methodRef(Function16<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef15<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R> ComponentMethodRef16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R> methodRef(Function17<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef16<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, R> ComponentMethodRef17<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, R> methodRef(Function18<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef17<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, R> ComponentMethodRef18<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, R> methodRef(Function19<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef18<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R> ComponentMethodRef19<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R> methodRef(Function20<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef19<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, R> ComponentMethodRef20<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, R> methodRef(Function21<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef20<>(kalixClient, methodRef, List.of(), callMetadata);
  }

  /**
   * Pass in an Action method reference annotated as a REST endpoint, e.g. <code>MyAction::create</code>
   */
  public <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, R> ComponentMethodRef21<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, R> methodRef(Function22<T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, Action.Effect<R>> methodRef) {
    return new ComponentMethodRef21<>(kalixClient, methodRef, List.of(), callMetadata);
  }
}
