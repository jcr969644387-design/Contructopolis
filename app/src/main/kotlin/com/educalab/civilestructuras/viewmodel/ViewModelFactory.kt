package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.educalab.civilestructuras.AppContainer

/**
 * Factory manual y genérica: evita repetir un Factory por cada ViewModel.
 * Cada ViewModel recibe el AppContainer y (opcionalmente) argumentos extra
 * de navegación a través de un lambda de construcción.
 */
class GenericViewModelFactory<T : ViewModel>(
    private val create: (AppContainer) -> T,
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
        return create(container) as VM
    }
}
