package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vennilay.kernvox.data.storage.AppSettingsRepository

class DetailViewModelFactory(
    private val application: Application,
    private val serverId: Int,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return DetailViewModel(application, serverId).cast()
    }
}

class ServersViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ServersViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return ServersViewModel(application).cast()
    }
}

class SettingsViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return SettingsViewModel(AppSettingsRepository(application)).cast()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.cast(): T = this as T
