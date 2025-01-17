/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.http;

import akka.annotation.DoNotInherit;
import akka.javasdk.Context;
import akka.javasdk.JwtClaims;
import akka.javasdk.Principals;
import akka.javasdk.Tracing;

/**
 * Not for user extension, can be injected as constructor parameter into HTTP endpoint components or
 * accessible from {@link AbstractHttpEndpoint#requestContext()} if the endpoint class extends
 * `AbstractHttpEndpoint`.
 */
@DoNotInherit
public interface RequestContext extends Context {

  /**
   * Get the principals associated with this request.
   *
   * @return The principals associated with this request.
   */
  Principals getPrincipals();

  /** @return The JWT claims, if any, associated with this request. */
  JwtClaims getJwtClaims();

  /** Access to tracing for custom app specific tracing. */
  Tracing tracing();
}
