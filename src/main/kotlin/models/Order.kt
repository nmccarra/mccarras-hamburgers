package models

data class Order(
    val id: String,
    val burgerQuantity: Int,
    val drinkQuantity: Int
)