package com.courseflow.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courseflow.data.remote.AuthRepository
import com.courseflow.domain.InputValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null
)

sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // One-shot navigation event: a Channel avoids re-navigating on rotation.
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginClicked(email: String, password: String) {
        if (_state.value.isLoading) return

        val emailError = if (InputValidator.isValidEmail(email)) null else "Enter a valid email address"
        val passwordError = if (InputValidator.isValidPassword(password)) null
        else "Password must be at least ${InputValidator.MIN_PASSWORD_LENGTH} characters"
        if (emailError != null || passwordError != null) {
            _state.value = LoginUiState(emailError = emailError, passwordError = passwordError)
            return
        }

        _state.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            auth.login(email.trim(), password)
                .onSuccess {
                    _state.value = LoginUiState()
                    _events.send(LoginEvent.NavigateToDashboard)
                }
                .onFailure { _state.value = LoginUiState(formError = it.message ?: "Login failed") }
        }
    }
}
