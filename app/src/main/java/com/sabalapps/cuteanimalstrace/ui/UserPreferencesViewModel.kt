package com.sabalapps.cuteanimalstrace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sabalapps.cuteanimalstrace.data.*
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Keeps local writes alive across navigation and configuration changes. */
class UserPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application.userPreferencesStore)
    private val _catalog = MutableStateFlow<LocalTemplateCatalog?>(null)
    val catalog = _catalog.asStateFlow()
    private val _preferences = MutableStateFlow<UserPreferences?>(null)
    val preferences = _preferences.asStateFlow()
    private val _loadFailed = MutableStateFlow(false)
    val loadFailed = _loadFailed.asStateFlow()
    private val _writeFailed = MutableStateFlow(false)
    val writeFailed = _writeFailed.asStateFlow()
    private var readJob: Job? = null

    init { load() }

    fun load() {
        readJob?.cancel()
        _loadFailed.value = false
        readJob = viewModelScope.launch {
            try {
                if (_catalog.value == null) {
                    _catalog.value = withContext(Dispatchers.IO) {
                        LocalTemplateCatalog.load(getApplication<Application>().assets)
                    }
                }
                repository.data.collect { _preferences.value = it }
            }
            catch (_: IOException) { _loadFailed.value = true }
        }
    }

    fun dismissWriteError() { _writeFailed.value = false }
    fun update(action: suspend UserPreferencesRepository.() -> Unit) {
        viewModelScope.launch {
            try { repository.action() }
            catch (_: IOException) { _writeFailed.value = true }
        }
    }
}
