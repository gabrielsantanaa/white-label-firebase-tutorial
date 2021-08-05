package br.com.douglasmotta.whitelabeltutorial.ui.addproduct

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.douglasmotta.whitelabeltutorial.R
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

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventChannel = _eventChannel.receiveAsFlow()

    private var _formIsValid = false

    sealed class Event {

        abstract val resId: Int
        abstract var isValid: Boolean

        class ImageUriErrorResId(override var isValid: Boolean) : Event() {
            override val resId: Int
                get() = R.drawable.background_product_image_error
        }

        class DescriptionFieldErrorResId(override var isValid: Boolean) : Event() {
            override val resId: Int
                get() = R.string.add_product_description_field_error
        }

        class PriceFieldErrorResId(override var isValid: Boolean) : Event() {
            override val resId: Int
                get() = R.string.add_product_price_field_error
        }
    }

    fun validateFields(description: String, price: String, imageUri: Uri?) = viewModelScope.launch {

        validateStringField(description, Event.DescriptionFieldErrorResId(false))
        validateStringField(price, Event.PriceFieldErrorResId(false))
        validateUriField(imageUri, Event.ImageUriErrorResId(false))

        if(_formIsValid) createProduct(description, price, imageUri!!)
    }

    private suspend fun validateStringField(field: String, event: Event) {
        event.isValid = field.isNotEmpty()
        _formIsValid = field.isNotEmpty()
        _eventChannel.send(event)
    }


    private suspend fun validateUriField(field: Uri?, event: Event) {
        event.isValid = field != null
        _formIsValid = field != null
        _eventChannel.send(event)
    }


    private suspend fun createProduct(description: String, price: String, imageUri: Uri) =
        try {
            val product = createProductUseCase(description, price.fromCurrency(), imageUri)
        } catch (exception: Exception) {
            Log.d("error", "createProduct: $exception")
        }

}