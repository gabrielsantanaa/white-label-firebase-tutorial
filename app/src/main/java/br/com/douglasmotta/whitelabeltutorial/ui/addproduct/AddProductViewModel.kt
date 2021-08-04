package br.com.douglasmotta.whitelabeltutorial.ui.addproduct

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.douglasmotta.whitelabeltutorial.R
import br.com.douglasmotta.whitelabeltutorial.domain.usecase.CreateProductUseCase
import br.com.douglasmotta.whitelabeltutorial.util.fromCurrency
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddProductViewModel(
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventChannel = _eventChannel.receiveAsFlow()

    sealed class Event {

        abstract val resId: Int

        object ImageUriErrorResId : Event() {
            override val resId: Int
                get() = R.drawable.background_product_image_error
        }

        object DescriptionFieldErrorResId : Event() {
            override val resId: Int
                get() = R.string.add_product_description_field_error
        }

        object PriceFieldErrorResId : Event() {
            override val resId: Int
                get() = R.string.add_product_price_field_error
        }
    }

    fun validateFields(description: String, price: String, imageUri: Uri?) = viewModelScope.launch {
        if (
            validateStringField(description, Event.DescriptionFieldErrorResId) &&
            validateStringField(price, Event.PriceFieldErrorResId) &&
            validateUriField(imageUri, Event.ImageUriErrorResId)
        ) {
            createProduct(description, price, imageUri!!)
        }
    }

    private suspend fun validateStringField(field: String, event: Event): Boolean =
        if (field.isEmpty()) {
            _eventChannel.send(event)
            false
        } else true


    private suspend fun validateUriField(field: Uri?, event: Event): Boolean =
        if (field == null) {
            _eventChannel.send(event)
            false
        } else true


    private suspend fun createProduct(description: String, price: String, imageUri: Uri) =
        try {
            val product = createProductUseCase(description, price.fromCurrency(), imageUri)
        } catch (exception: Exception) {
            Log.d("error", "createProduct: $exception")
        }

}