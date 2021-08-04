package br.com.douglasmotta.whitelabeltutorial.ui.addproduct

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import br.com.douglasmotta.whitelabeltutorial.databinding.AddProductFragmentBinding
import br.com.douglasmotta.whitelabeltutorial.util.CurrencyTextWatcher
import br.com.douglasmotta.whitelabeltutorial.util.observeOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout


class AddProductFragment : BottomSheetDialogFragment() {

    private var _binding: AddProductFragmentBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            binding.imageProduct.setImageURI(uri)
        }

    private lateinit var viewModel: AddProductViewModel

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
                AddProductViewModel.Event.DescriptionFieldErrorResId -> {
                    binding.textInputLayoutDescription.setError(event.resId)
                }
                AddProductViewModel.Event.ImageUriErrorResId -> {
                    binding.imageProduct.setImageResource(event.resId)
                }
                AddProductViewModel.Event.PriceFieldErrorResId -> {
                    binding.textInputLayoutDescription.setError(event.resId)
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

    private fun TextInputLayout.setError(stringResId: Int) {
        error = getString(stringResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}