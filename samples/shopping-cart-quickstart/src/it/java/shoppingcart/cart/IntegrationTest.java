package shoppingcart.cart;

import akka.javasdk.testkit.TestKitSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shoppingcart.application.ShoppingCartEntity;
import shoppingcart.domain.ShoppingCart;
import shoppingcart.domain.ShoppingCart.LineItem;




public class IntegrationTest extends TestKitSupport {

  @Test
  public void createAndManageCart() {

    String cartId = "card-abc";

    var item1 = new LineItem("tv", "Super TV 55'", 1);

    var response1 = await(
      componentClient
        .forEventSourcedEntity(cartId)
        .method(ShoppingCartEntity::addItem)
        .invokeAsync(item1)
    );
    Assertions.assertNotNull(response1);

    var item2 = new LineItem("tv-table", "Table for TV", 1);

    var response2 = await(
      componentClient
        .forEventSourcedEntity(cartId)
        .method(ShoppingCartEntity::addItem)
        .invokeAsync(item2)
    );
    Assertions.assertNotNull(response2);


    ShoppingCart cartInfo = await(
      componentClient
        .forEventSourcedEntity(cartId)
        .method(ShoppingCartEntity::getCart)
        .invokeAsync()
    );
    Assertions.assertEquals(2, cartInfo.items().size());


    // removing one of the items
    var response3 =
      await(
        componentClient
          .forEventSourcedEntity(cartId)
          .method(ShoppingCartEntity::removeItem)
          .invokeAsync(item1.productId())
      );

    Assertions.assertNotNull(response3);

    // confirming only one product remains
    ShoppingCart cartUpdated = await(
      componentClient
        .forEventSourcedEntity(cartId)
        .method(ShoppingCartEntity::getCart).invokeAsync()
    );
    Assertions.assertEquals(1, cartUpdated.items().size());
    Assertions.assertEquals(item2, cartUpdated.items().get(0));
  }
}