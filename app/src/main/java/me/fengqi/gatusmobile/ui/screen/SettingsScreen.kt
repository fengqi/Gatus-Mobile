package me.fengqi.gatusmobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.fengqi.gatusmobile.ui.theme.GatusBackground
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusPrimary
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy
import me.fengqi.gatusmobile.ui.viewmodel.ValidationState

@Composable
fun SettingsScreen(
    currentUrl: String,
    validationState: ValidationState,
    onSave: (String) -> Unit,
    onUrlChanged: () -> Unit,
    isInitialSetup: Boolean = false
) {
    var url by remember { mutableStateOf(currentUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GatusBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isInitialSetup) "Welcome to Gatus" else "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your Gatus server URL to get started.\nThe app will fetch endpoint statuses from this server.",
            fontSize = 14.sp,
            color = GatusTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                if (validationState is ValidationState.Error) {
                    onUrlChanged()
                }
            },
            label = { Text("Server URL") },
            placeholder = { Text("https://status.example.com") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            isError = validationState is ValidationState.Error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GatusPrimary,
                unfocusedBorderColor = GatusCardBorder,
                focusedLabelColor = GatusPrimary,
                cursorColor = GatusPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (validationState is ValidationState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationState.message,
                fontSize = 13.sp,
                color = GatusUnhealthy,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(url) },
            enabled = url.isNotBlank() && validationState !is ValidationState.Loading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GatusPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (validationState is ValidationState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(20.dp)
                )
            } else {
                Text(
                    text = if (isInitialSetup) "Get Started" else "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
