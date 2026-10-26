package com.rango.delivery.feature.address

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.ApiFieldError
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.address.CreateAddressRequest
import com.rango.delivery.data.address.SavedAddressDto
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
class AddressViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAddressesWithSelectedOne() = runTest {
        val viewModel = AddressViewModel(FakeAddresses())

        val state = viewModel.uiState.value
        assertEquals(2, state.addresses.size)
        assertEquals(1L, state.selectedId)
        assertTrue(state.mapImageUrl!!.endsWith("/static/maps/area.jpg"))
    }

    @Test
    fun selectingUpdatesBackendAndShowsToast() = runTest {
        val repository = FakeAddresses()
        val viewModel = AddressViewModel(repository)

        viewModel.onSelect(2)

        assertEquals(2L, viewModel.uiState.value.selectedId)
        assertEquals(2L, repository.selected)
        assertEquals(AddressEvent.ShowToast("Endereço atualizado.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun selectFailureRestoresPrevious() = runTest {
        val viewModel = AddressViewModel(FakeAddresses(selectFailure = NetworkException(RuntimeException())))

        viewModel.onSelect(2)

        assertEquals(1L, viewModel.uiState.value.selectedId)
    }

    @Test
    fun saveSplitsDistrictAndClosesForm() = runTest {
        val repository = FakeAddresses()
        val viewModel = AddressViewModel(repository)
        viewModel.onToggleForm()
        viewModel.onFieldChange(AddressField.Cep, "01310100")
        viewModel.onFieldChange(AddressField.Street, "Av. Paulista, 1000")
        viewModel.onFieldChange(AddressField.District, "Bela Vista, São Paulo")

        viewModel.onSave()

        val request = repository.created!!
        assertEquals("Bela Vista", request.neighborhood)
        assertEquals("São Paulo", request.city)
        assertEquals(null, request.complement)
        assertFalse(viewModel.uiState.value.formOpen)
        assertEquals(3, viewModel.uiState.value.addresses.size)
        assertEquals(AddressEvent.ShowToast("Endereço salvo no dispositivo.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun validationErrorsGoToTheirFields() = runTest {
        val failure = ApiException(
            400,
            "VALIDATION_ERROR",
            "Dados inválidos.",
            listOf(
                ApiFieldError("cep", "CEP inválido."),
                ApiFieldError("neighborhood", "Informe o bairro."),
                ApiFieldError("city", "Informe a cidade."),
            ),
        )
        val viewModel = AddressViewModel(FakeAddresses(createFailure = failure))
        viewModel.onToggleForm()

        viewModel.onSave()

        val errors = viewModel.uiState.value.form.errors
        assertEquals("CEP inválido.", errors[AddressField.Cep])
        assertEquals("Informe o bairro.", errors[AddressField.District])
        assertTrue(viewModel.uiState.value.formOpen)
    }

    @Test
    fun useAddressEmitsDone() = runTest {
        val viewModel = AddressViewModel(FakeAddresses())

        viewModel.onUseAddress()

        assertEquals(AddressEvent.Done, viewModel.events.first())
    }

    private class FakeAddresses(
        private val selectFailure: Exception? = null,
        private val createFailure: Exception? = null,
    ) : AddressRepository {
        var selected: Long? = null
        var created: CreateAddressRequest? = null
        private val items = mutableListOf(
            address(1, "Casa", selected = true),
            address(2, "Trabalho", selected = false),
        )

        override suspend fun list() = items.toList()

        override suspend fun create(request: CreateAddressRequest): SavedAddressDto {
            createFailure?.let { throw it }
            created = request
            return address(3, request.label, selected = true).also { new ->
                items.replaceAll { it.copy(isSelected = false) }
                items += new
            }
        }

        override suspend fun select(addressId: Long): SavedAddressDto {
            selectFailure?.let { throw it }
            selected = addressId
            return items.first { it.id == addressId }
        }

        private fun address(id: Long, label: String, selected: Boolean) = SavedAddressDto(
            id = id,
            label = label,
            cep = "05422-030",
            street = "Rua das Palmeiras, 128",
            neighborhood = "Pinheiros",
            city = "São Paulo",
            line = "Rua das Palmeiras, 128",
            cityLine = "Pinheiros · São Paulo",
            isSelected = selected,
            mapImageUrl = "/static/maps/area.jpg",
        )
    }
}
