package com.rango.delivery.data.cart

import com.rango.delivery.data.catalog.AddonDto
import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    val productId: String,
    val quantity: Int = 1,
    val addonIds: List<String> = emptyList(),
    val notes: String = "",
)

@Serializable
data class UpdateCartItemRequest(val quantity: Int)

@Serializable
data class CouponRequest(val code: String)

@Serializable
data class CartProductDto(
    val id: String,
    val name: String,
    val restaurant: String,
    val imageUrl: String? = null,
    val imageHue: Int,
    val isAvailable: Boolean = true,
)

@Serializable
data class CartItemDto(
    val id: Long,
    val product: CartProductDto,
    val quantity: Int,
    val addons: List<AddonDto> = emptyList(),
    val notes: String = "",
    val unitPrice: Double,
    val total: Double,
)

@Serializable
data class CartCouponDto(
    val code: String,
    val description: String,
    val percentOff: Int,
    val minSubtotal: Double,
    val applied: Boolean,
)

@Serializable
data class CartSummaryDto(
    val itemCount: Int,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val freeDeliveryThreshold: Double = 0.0,
)

@Serializable
data class CartDto(
    val items: List<CartItemDto> = emptyList(),
    val coupon: CartCouponDto? = null,
    val summary: CartSummaryDto,
)
