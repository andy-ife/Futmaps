package com.andyslab.futmaps.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionDialog(
    permission: String,
    description: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onGoToAppSettingsClick : () -> Unit,
    modifier: Modifier = Modifier
){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                    onClick = { onGoToAppSettingsClick() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(0xFF672976),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(2.dp),
                    contentPadding = PaddingValues(2.dp)
                    ) {
                    Text(text = "Settings")
                }

        },
        modifier = modifier,
        title = {
            Text(text = "$permission Required",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp)
        },
        text = description,
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,

        )
}

@Preview
@Composable
fun PermissionDialogPreview(){
    PermissionDialog(
        permission = "Location",
        description = {
            Text(text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 16.sp)){
                    append("Futmaps can't work without knowing your precise location. Go to app " )
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)){ append("Settings")}
                    append(" to grant precise location access.")
                }
            })
        },
        onDismiss = { /*TODO*/ },
        onGoToAppSettingsClick = { /*TODO*/ })
}