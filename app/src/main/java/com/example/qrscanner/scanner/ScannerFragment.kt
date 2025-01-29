package com.example.qrscanner.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.qrscanner.R
import com.example.qrscanner.database.AppDatabase
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@ExperimentalGetImage
class ScannerFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ScannerViewModel by viewModels {
        ScannerViewModelFactory(
            AppDatabase.getInstance(requireContext()), // База данных
            requireContext()                           // Контекст
        )
    }
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var previewView: PreviewView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)

        // Проверка разрешений камеры
        if (allPermissionsGranted()) {
            setupCamera()
        } else {
            requestPermissions.launch(Manifest.permission.CAMERA)
        }
        // Настраиваем камеру
        setupCamera()

        // Наблюдаем за результатами обработки
        viewModel.scannedResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ScannerViewModel.ScanResult.Valid -> {
                    Toast.makeText(requireContext(),
                        "Код добавлен в базу", Toast.LENGTH_SHORT).show()
                }
                is ScannerViewModel.ScanResult.Invalid -> {
                    Toast.makeText(requireContext(),
                        "Некорректный код: ${result.reason}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Проверка разрешений
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            setupCamera()
        } else {
            Toast.makeText(requireContext(), "Камера не разрешена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val analysisUseCase = ImageAnalysis.Builder().build().also { analysis ->
                analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                    processImage(imageProxy, barcodeScanner)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                analysisUseCase
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImage(imageProxy: ImageProxy, barcodeScanner: BarcodeScanner) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        viewModel.handleScannedCode(rawValue)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}



