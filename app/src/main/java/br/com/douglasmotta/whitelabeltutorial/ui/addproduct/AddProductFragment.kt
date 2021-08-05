package br.com.douglasmotta.whitelabeltutorial.ui.addproduct

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import br.com.douglasmotta.whitelabeltutorial.R
import br.com.douglasmotta.whitelabeltutorial.databinding.AddProductFragmentBinding
import br.com.douglasmotta.whitelabeltutorial.util.CurrencyTextWatcher
import br.com.douglasmotta.whitelabeltutorial.util.observeOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddProductFragment : BottomSheetDialogFragment() {

    private var _binding: AddProductFragmentBinding? = null

    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            binding.imageProduct.setImageURI(uri)
        }

    private val viewModel: AddProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEventsObserver()
        setListeners()
    }

    private fun setEventsObserver() {
        viewModel.eventChannel.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                is AddProductViewModel.Event.DescriptionFieldErrorResId -> {
                    if (event.isValid) {
                        binding.textInputLayoutDescription.error = ""
                    } else {
                        binding.textInputLayoutDescription.error = getString(event.resId)
                    }

                }
                is AddProductViewModel.Event.ImageUriErrorResId -> {
                    if (event.isValid) {
                        binding.imageProduct.setBackgroundResource(R.drawable.background_product_image)
                    } else {
                        binding.imageProduct.setBackgroundResource(R.drawable.background_product_image_error)
                    }

                }
                is AddProductViewModel.Event.PriceFieldErrorResId -> {
                    if (event.isValid) {
                        binding.textInputLayoutPrice.error = ""
                    } else {
                        binding.textInputLayoutPrice.error = getString(event.resId)
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.imageProduct.setOnClickListener {
            chooseImage()
        }
        binding.buttonAddProduct.setOnClickListener {
            val description = binding.textDescription.text.toString()
            val price = binding.textPrice.text.toString()
            viewModel.validateFields(description, price, imageUri)
        }
        binding.textPrice.run {
            addTextChangedListener(CurrencyTextWatcher(this))
        }
    }

    private fun chooseImage() {
        getContent.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}