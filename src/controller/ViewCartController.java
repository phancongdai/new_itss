package controller;

import java.sql.SQLException;
import java.util.List;

import entity.media.Media;
import entity.cart.Cart;
import entity.cart.CartMedia;

/**
 * This class controls the flow of events when users view the Cart
 * @author nguyendai
 */
public class ViewCartController extends BaseController{
    
    /**
     * This method checks the available products in Cart
     */
    public void checkAvailabilityOfProduct() {
        Cart.getCart().checkAvailabilityOfProduct();
    }

    /**
     * This method calculates the cart subtotal
     * @return subtotal
     * Data Coupling
     */
    public int getCartSubtotal(){
        return Cart.getCart().calSubtotal();
    }
}
