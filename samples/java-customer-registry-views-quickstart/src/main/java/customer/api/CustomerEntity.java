/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package customer.api;

import customer.domain.Address;
import customer.domain.Customer;
import akka.platform.javasdk.valueentity.ValueEntity;
import akka.platform.javasdk.annotations.TypeId;

@TypeId("customer")
public class CustomerEntity extends ValueEntity<Customer> {


  public record Ok() {
    public static final Ok instance = new Ok();
  }

  public ValueEntity.Effect<Ok> create(Customer customer) {
    return effects().updateState(customer).thenReply(Ok.instance);
  }

  public ValueEntity.Effect<Customer> getCustomer() {
    return effects().reply(currentState());
  }

  public Effect<Ok> changeName(String newName) {
    Customer updatedCustomer = currentState().withName(newName);
    return effects().updateState(updatedCustomer).thenReply(Ok.instance);
  }

  public Effect<Ok> changeAddress(Address newAddress) {
    Customer updatedCustomer = currentState().withAddress(newAddress);
    return effects().updateState(updatedCustomer).thenReply(Ok.instance);
  }

}