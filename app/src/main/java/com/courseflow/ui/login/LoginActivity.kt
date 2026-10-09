package com.courseflow.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.courseflow.R
import com.courseflow.ServiceLocator
import com.courseflow.databinding.ActivityLoginBinding
import com.courseflow.ui.ViewModelFactory
import com.courseflow.ui.courses.CourseListActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory { LoginViewModel(ServiceLocator.authRepository(applicationContext)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already logged in (works offline too): skip the login screen entirely.
        if (ServiceLocator.authRepository(applicationContext).isLoggedIn()) {
            openDashboard()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            viewModel.onLoginClicked(
                binding.emailInput.text?.toString().orEmpty().trim(),
                binding.passwordInput.text?.toString().orEmpty()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            LoginEvent.NavigateToDashboard -> openDashboard()
                        }
                    }
                }
            }
        }
    }

    private fun openDashboard() {
        startActivity(Intent(this, CourseListActivity::class.java))
        finish()
    }

    private fun render(state: LoginUiState) = with(binding) {
        loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !state.isLoading
        loginButton.text = if (state.isLoading) "" else getString(R.string.login)
        emailLayout.error = state.emailError
        passwordLayout.error = state.passwordError
        errorText.text = state.formError
        errorText.visibility = if (state.formError != null) View.VISIBLE else View.GONE
    }
}
