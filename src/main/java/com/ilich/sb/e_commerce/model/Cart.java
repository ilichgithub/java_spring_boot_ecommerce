package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación OneToOne con User: Un usuario tiene un carrito, y un carrito pertenece a un usuario.
    // Usamos JoinColumn para especificar la columna de clave foránea.
    // CascadeType.ALL significa que las operaciones en Cart (persistencia, eliminación) se propagan al User asociado.
    // OrphanRemoval = true asegura que si un Cart se desasocia de un User, el User también se elimina (¡cuidado con esto si User tiene otras relaciones!).
    // En un caso real, es más común que el User exista independientemente, así que considera si necesitas CascadeType.ALL y orphanRemoval.
    // Para este caso, solo mapearemos la relación sin cascada excesiva para evitar borrados accidentales de usuarios.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // user_id será la FK y debe ser única
    private User user;

    // Relación OneToMany con CartItem: Un carrito puede tener muchos ítems.
    // mappedBy indica el campo en la entidad CartItem que posee la relación.
    // CascadeType.ALL significa que si se guarda/elimina un Cart, sus CartItems asociados también se guardan/eliminan.
    // orphanRemoval = true significa que si un CartItem se elimina de la colección cartItems, también se elimina de la base de datos.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>(); // Usamos Set para evitar duplicados y mejor rendimiento

    // Constructor vacío requerido por JPA
    public Cart() {
    }

    // Constructor para crear un carrito asociado a un usuario
    public Cart(User user) {
        this.user = user;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    // Métodos de conveniencia para añadir/eliminar ítems
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this); // Asegura la bidireccionalidad
    }

    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null); // Desasocia el ítem del carrito
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return id != null && id.equals(cart.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}