package br.com.douglasmotta.whitelabeltutorial.ui.addproduct

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.douglasmotta.whitelabeltutorial.R
import br.com.douglasmotta.whitelabeltutorial.domain.model.Product
import br.com.douglasmotta.whitelabeltutorial.domain.usecase.CreateProductUseCase
import br.com.douglasmotta.whitelabeltutorial.util.fromCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    sealed class Event {

        class ImageUriErrorResId(var isValid: Boolean) : Event() {
            val resId = R.drawable.background_product_image_error
        }
        class DescriptionFieldErrorResId(var isValid: Boolean) : Event() {
            val resId = R.string.add_product_description_field_error
        }
        class PriceFieldErrorResId(var isValid: Boolean) : Event() {
            val resId = R.string.add_product_price_field_error
        }
        class SuccessfullyCreatedProduct(val product: Product) : Event() {
            val resId = R.string.add_product_successfully_created_product
        }
    }

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventChannel = _eventChannel.receiveAsFlow()

    private var _formIsValid = false

    fun validateFields(description: String, price: String, imageUri: Uri?) = viewModelScope.launch {

        validateDescriptionField(description)
        validatePriceField(price)
        validateImageField(imageUri)

        if(_formIsValid) createProduct(description, price, imageUri!!)
    }

    private suspend fun validateDescriptionField(field: String) {
        _formIsValid = field.isNotEmpty()
        _eventChannel.send(
            Event.DescriptionFieldErrorResId(field.isNotEmpty())
        )
    }
    private suspend fun validatePriceField(field: String) {
        _formIsValid = field.isNotEmpty()
        _eventChannel.send(
            Event.PriceFieldErrorResId(field.isNotEmpty())
        )
    }
    private suspend fun validateImageField(field: Uri?) {
        _formIsValid = field != null
        _eventChannel.send(
            Event.ImageUriErrorResId(field != null)
        )
    }


    private suspend fun createProduct(description: String, price: String, imageUri: Uri) =
        try {
            val product = createProductUseCase(description, price.fromCurrency(), imageUri)
            _eventChannel.send(
                Event.SuccessfullyCreatedProduct(product)
            )
        } catch (exception: Exception) {
            Log.d("error", "createProduct: $exception")
        }

}