package com.andyslab.futmaps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.andyslab.futmaps.R

@Composable
fun ErrorDialog(modifier: Modifier = Modifier,
                message: String,
                mainAction: String = "Retry",
                onDismiss: () -> Unit,
                onMainActionClick: () -> Unit,){
    Dialog(
        onDismissRequest = {onDismiss()},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Card(
            modifier = modifier
                .padding(vertical = 10.dp, horizontal = 14.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(5.dp),

            ) {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp)){
            Text(
                modifier = Modifier.padding(8.dp),
                text = message,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start)
            }



            Row (modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
                horizontalArrangement = Arrangement.Start){

                TextButton(
                    onClick = { onMainActionClick() },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF7B1FA2),
                    ),
                    //elevation = ButtonDefaults.buttonElevation(2.dp),
                ) {

                    Text(text = mainAction,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)))
                }
            }

        }
    }
}

@Composable
@Preview
fun ErrorDialogPreview(){
    ErrorDialog(message = "Location not found. It might not have been uploaded yet.", onDismiss = {}) {
        
    }
}
