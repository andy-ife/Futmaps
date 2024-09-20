package com.andyslab.futmaps.ui.showroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyslab.futmaps.R
import com.andyslab.futmaps.domain.entities.TripData
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun StartNavigationBottomSheet(
    modifier: Modifier = Modifier,
    tripState: TripData = TripData(),
    cancel: () -> Unit = {},
    startNav: () -> Unit = {}
){
    val desc = if(!tripState.driveMode)"On foot" else "In vehicle"
    val iconId = if(!tripState.driveMode)R.drawable.walk_icon_filled else R.drawable.drive_icon_filled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        ) {

        Text(
            text = "Fastest route",
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.sourcesans3_extralight_italic)),
            color = Color(0xFF606060),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){

            Row(modifier = Modifier.wrapContentSize(),){

            Icon(
                painter = painterResource(iconId),
                contentDescription = "walk",
                modifier = Modifier
                    .size(34.dp)
                    .offset(x = -8.dp, y = 4.dp))

            Column(modifier = Modifier
                .wrapContentSize()
                .offset(x = -4.dp)){
                Text(text = tripState.timeToDest.toMinutes().toString() + " min",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold)

                Text(
                    text = desc,
                    fontFamily = FontFamily(Font(R.font.sourcesans3_extralight_italic)),
                    color = Color(0xFF606060),
                    fontWeight = FontWeight.Bold)

            }

            }

            Text(text = tripState.distanceToDest.toKilometres().toString() + " km",
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            ){

            TextButton(onClick = { cancel() },
                modifier = Modifier.fillMaxWidth(0.45f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color(0xFF672976)
                )
            ) {
                Text(text = "Cancel",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = { startNav() },
                modifier = Modifier.fillMaxWidth(0.811f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF672976),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text(text = "Start",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold)
            }
        }

    }
}

@Composable
@Preview
fun ShowRouteBottomSheetPreview(){
    StartNavigationBottomSheet()
}

fun Double.toMinutes(): Int{
    return (this.roundToInt() + 30) / 60
}

fun Double.toKilometres(): Double{
    return((this/1000) * 10).roundToInt() / 10.0
}
