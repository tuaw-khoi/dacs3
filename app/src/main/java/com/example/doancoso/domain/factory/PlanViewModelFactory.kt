package com.example.doancoso.domain.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doancoso.data.repository.PlanRepository
import com.example.doancoso.domain.PlanViewModel

class PlanViewModelFactory(
    private val planRepository: PlanRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanViewModel::class.java)) {
            return PlanViewModel(planRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}