package com.andyslab.futmaps.ui.editlocations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyslab.futmaps.R
import kotlinx.coroutines.launch
import java.time.format.TextStyle

@Composable
fun EditLocationsBottomSheet(
    modifier: Modifier = Modifier,
    name: MutableState<String>,
    shortDesc: MutableState<String>,
    lat: MutableState<Double>,
    long: MutableState<Double>,
    readOnly:Boolean = true,
    onSaveClick: () -> Unit = {}
    ){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
            .padding(start = 10.dp, end = 10.dp, bottom = 60.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp,),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)){
        OutlinedTextField(
            value = name.value,
            modifier = Modifier.fillMaxWidth(0.48f),
            onValueChange = {name.value = it},
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
            label = { Text(text = "Name",fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 14.sp)},
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = shortDesc.value,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {shortDesc.value = it},
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
            label = { Text(text = "Short Description",fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 14.sp)},
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )
    }

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)){
        OutlinedTextField(
            value = lat.value.toString().let{
                if(it == "0.0") "" else it
            },
            modifier = Modifier.fillMaxWidth(0.48f),
            onValueChange = {
                try{
                lat.value = it.toDouble()
                }catch(e: NumberFormatException){
                }
                            },
            readOnly = readOnly,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
            label = { Text(text = "Latitude",fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 14.sp)},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = long.value.toString().let{
                if(it == "0.0") "" else it
                                             },
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {try{
                long.value = it.toDouble()
            }catch(e: NumberFormatException){

            }},
            readOnly = readOnly,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
            label = { Text(text = "Longitude",
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 14.sp
            )},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )
        }

        TextButton(
            onClick = {onSaveClick()},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF672976),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "Save",
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

    }
}

@Composable
@Preview
fun BottomSheetPrevTwo(){
    EditLocationsBottomSheet(
        Modifier,
        remember{mutableStateOf("Ultramodern Market")},
        remember{mutableStateOf("A collection of student-friendly stores")},
        remember{mutableStateOf(9.0243929384579)},
        remember{mutableStateOf(6.847523759723,)})
}