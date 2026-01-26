package org.example.shared.model.DTO;

import lombok.Data;
import org.example.shared.model.entity.Product;

@Data
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getSubTotal() {
        return product.getEffectivePrice() * quantity;
    }
}
