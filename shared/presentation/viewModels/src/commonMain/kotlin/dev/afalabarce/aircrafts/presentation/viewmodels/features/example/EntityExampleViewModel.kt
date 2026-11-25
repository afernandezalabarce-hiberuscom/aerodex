package dev.afalabarce.aircrafts.presentation.viewmodels.features.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afalabarce.aircrafts.domain.usecases.features.example.GetEntitiesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

class EntityExampleViewModel(private val getEntitiesUseCase: GetEntitiesUseCase): ViewModel() {

    val entities by lazy { getEntitiesUseCase().shareIn(viewModelScope, SharingStarted.Lazily) }
}
