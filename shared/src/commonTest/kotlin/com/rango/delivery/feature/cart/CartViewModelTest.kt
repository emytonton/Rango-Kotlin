package com.rango.delivery.feature.cart

import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.cart.CartCouponDto
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartItemDto
import com.rango.delivery.data.cart.CartProductDto
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.cart.CartSummaryDto
import com.rango.delivery.data.catalog.AddonDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsLinesAndSummary() = runTest {
        val viewModel = CartViewModel(FakeCart())

        val state = viewModel.uiState.value
        val line = state.lines.single()
        assertEquals("R$ 38,90", line.unitPrice)
        assertEquals("Bacon extra", line.addons)
        assertEquals("sem cebola", line.notes)
        assertEquals("R$ 77,80", line.total)
        assertEquals(2, state.itemCount)
        assertEquals(SummaryLine("Taxa de entrega", "Grátis", highlight = true), state.summary[1])
    }

    @Test
    fun increaseSendsNextQuantity() = runTest {
        val cart = FakeCart()
        val viewModel = CartViewModel(cart)

        viewModel.onIncrease(10)

        assertEquals(10L to 3, cart.lastQuantity)
        assertEquals(3, viewModel.uiState.value.lines.single().quantity)
    }

    @Test
    fun decreasingLastUnitRemovesLine() = runTest {
        val cart = FakeCart(quantity = 1)
        val viewModel = CartViewModel(cart)

        viewModel.onDecrease(10)

        assertEquals(10L, cart.removed)
        assertTrue(viewModel.uiState.value.empty)
    }

    @Test
    fun removeShowsWarningToast() = runTest {
        val viewModel = CartViewModel(FakeCart())

        viewModel.onRemove(10)

        assertEquals(CartEvent.ShowToast("Produto removido.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun clearEmptiesCart() = runTest {
        val viewModel = CartViewModel(FakeCart())

        viewModel.onClear()

        assertTrue(viewModel.uiState.value.empty)
        assertEquals(CartEvent.ShowToast("Carrinho esvaziado.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun validCouponAddsDiscountLine() = runTest {
        val cart = FakeCart()
        val viewModel = CartViewModel(cart)

        viewModel.onCouponChange("rango10")
        viewModel.onApplyCoupon()

        val state = viewModel.uiState.value
        assertTrue(state.couponApplied)
        assertEquals("RANGO10", state.couponInput)
        assertEquals("RANGO10", cart.lastCoupon)
        assertEquals(SummaryLine("Desconto · RANGO10", "- R$ 7,78", highlight = true), state.summary.last())
        assertEquals(CartEvent.ShowToast("Cupom RANGO10 aplicado · 10% off.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun invalidCouponShowsBackendMessage() = runTest {
        val viewModel = CartViewModel(FakeCart())

        viewModel.onCouponChange("XYZ")
        viewModel.onApplyCoupon()

        assertFalse(viewModel.uiState.value.couponApplied)
        assertEquals(CartEvent.ShowToast("Cupom inválido.", ToastKind.Error), viewModel.events.first())
    }

    private class FakeCart(private var quantity: Int = 2) : CartRepository {
        var lastQuantity: Pair<Long, Int>? = null
        var removed: Long? = null
        var lastCoupon: String? = null
        private var empty = false
        private var coupon = false

        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String) = error("não usado")
        override suspend fun cart() = dto()
        override suspend fun updateQuantity(itemId: Long, quantity: Int): CartDto {
            lastQuantity = itemId to quantity
            this.quantity = quantity
            return dto()
        }
        override suspend fun removeItem(itemId: Long): CartDto {
            removed = itemId
            empty = true
            return dto()
        }
        override suspend fun clear(): CartDto {
            empty = true
            return dto()
        }
        override suspend fun applyCoupon(code: String): CartDto {
            lastCoupon = code
            if (code != "RANGO10") throw ApiException(422, "INVALID_COUPON", "Cupom inválido.")
            coupon = true
            return dto()
        }

        private fun dto(): CartDto {
            if (empty) return CartDto(summary = CartSummaryDto(itemCount = 0))
            val subtotal = 38.9 * quantity
            val discount = if (coupon) 7.78 else 0.0
            return CartDto(
                items = listOf(
                    CartItemDto(
                        id = 10,
                        product = CartProductDto("p1", "Burger duplo cheddar", "Casa do Burger", null, 48),
                        quantity = quantity,
                        addons = listOf(AddonDto("a1", "Bacon extra", 4.0)),
                        notes = "sem cebola",
                        unitPrice = 38.9,
                        total = subtotal,
                    ),
                ),
                coupon = if (coupon) CartCouponDto("RANGO10", "10% de desconto", 10, 0.0, applied = true) else null,
                summary = CartSummaryDto(
                    itemCount = quantity,
                    subtotal = subtotal,
                    deliveryFee = 0.0,
                    discount = discount,
                    total = subtotal - discount,
                    freeDeliveryThreshold = 60.0,
                ),
            )
        }
    }
}
