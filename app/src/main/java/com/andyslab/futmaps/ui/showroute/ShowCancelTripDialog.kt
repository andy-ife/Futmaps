package com.andyslab.futmaps.ui.showroute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.andyslab.futmaps.R

@Composable
fun ShowCancelTripDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onCancelTripClick: () -> Unit = {},
){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = { onCancelTripClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF672976),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(2.dp),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text(text = "Stop Navigation")
            }

        },
        modifier = modifier,
        title = {
            Text(text = "Exit Navigation",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp)
        },
        text = {
            Text(text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 16.sp)){
                    append("Do you want to stop navigation?" )
                }
            })
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
        )
}

@Composable
@Preview
fun PreviewDialog(){
    ShowCancelTripDialog()
}