package com.rango.delivery.data.catalog

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val user: HomeUserDto,
    val selectedAddress: AddressDto? = null,
    val cartItemCount: Int,
    val unreadNotifications: Int,
    val categories: List<CategoryDto>,
    val sections: List<HomeSectionDto>,
)

@Serializable
data class HomeUserDto(val name: String, val firstName: String, val avatarUrl: String? = null)

@Serializable
data class AddressDto(val id: Long, val label: String, val line: String, val cityLine: String)

@Serializable
data class CategoryDto(val id: Long = 0, val name: String, val slug: String)

@Serializable
data class HomeSectionDto(val key: String, val title: String, val products: List<ProductSummaryDto>)

@Serializable
data class ProductSummaryDto(
    val id: String,
    val name: String,
    val restaurant: String,
    val category: CategoryDto,
    val description: String,
    val price: Double,
    val oldPrice: Double? = null,
    val rating: Double,
    val deliveryTime: DeliveryTimeDto,
    val imageHue: Int,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false,
)

@Serializable
data class DeliveryTimeDto(val min: Int, val max: Int)

@Serializable
data class ProductPageDto(val data: List<ProductSummaryDto>)

@Serializable
data class FavoriteStateDto(val productId: String, val isFavorite: Boolean)

@Serializable
data class AddonDto(val id: String, val name: String, val price: Double)

@Serializable
data class ProductDetailDto(
    val id: String,
    val name: String,
    val restaurant: String,
    val category: CategoryDto,
    val description: String,
    val price: Double,
    val oldPrice: Double? = null,
    val rating: Double,
    val deliveryTime: DeliveryTimeDto,
    val imageHue: Int,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false,
    val ingredients: String,
    val addons: List<AddonDto> = emptyList(),
)
