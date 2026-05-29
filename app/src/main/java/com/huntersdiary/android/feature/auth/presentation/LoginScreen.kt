package com.huntersdiary.android.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    AuthScreenLayout(
        title = "Вход",
        email = state.email,
        password = state.password,
        errorMessage = state.errorMessage,
        isLoading = state.isLoading,
        primaryButtonText = "Войти",
        secondaryButtonText = "Зарегистрироваться",
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
        onPrimaryClick = onLogin,
        onSecondaryClick = onRegisterClick,
    )
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onLoginClick: () -> Unit,
) {
    AuthScreenLayout(
        title = "Регистрация",
        email = state.email,
        password = state.password,
        errorMessage = state.errorMessage,
        isLoading = state.isLoading,
        primaryButtonText = "Зарегистрироваться",
        secondaryButtonText = "Уже есть аккаунт",
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
        onPrimaryClick = onRegister,
        onSecondaryClick = onLoginClick,
    )
}

@Composable
private fun AuthScreenLayout(
    title: String,
    email: String,
    password: String,
    errorMessage: String?,
    isLoading: Boolean,
    primaryButtonText: String,
    secondaryButtonText: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Дневник охотника",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Электронная почта") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(if (isPasswordVisible) "Скрыть" else "Показать")
                }
            },
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            Text(if (isLoading) "Отправка..." else primaryButtonText)
        }
        TextButton(
            onClick = onSecondaryClick,
            enabled = !isLoading,
        ) {
            Text(secondaryButtonText)
        }
    }
}
