package com.andyslab.futmaps.ui.showroute

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
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
import com.andyslab.futmaps.data.NavigationTools
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.TripData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TripProgressBottomSheet(
    modifier: Modifier = Modifier,
    tripData: TripData = TripData(),
    onFlagPressed: () -> Unit = {},
    onCancel: () -> Unit = {},
){
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, tripData.timeToDest.toMinutes())
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(calendar.time)

    Column(modifier = Modifier.fillMaxSize()){
    Row (modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp).offset(y=-12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {onFlagPressed()},
            modifier = Modifier.border(1.dp, Color.DarkGray, CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xff672976)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_outlined_flag_24),
                contentDescription = "marker",
                tint = Color(0xFF672976)
            )
        }

        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tripData.timeToDest.toMinutes().toString() + " min",
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xff388e3c)
            )
            Text(
                text = """${tripData.distanceToDest.toKilometres()} km  |  Arrival at $formattedTime""",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.sourcesans_3_extra_light)),
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(
            onClick = {onCancel()},
            modifier = Modifier.border(1.dp, Color.DarkGray, CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xff672976)
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "marker",
                tint = Color.Red
            )
        }
        }
}
    }


@Composable
@Preview
fun TripProgressBottomSheetPrev(){
    TripProgressBottomSheet(tripData = TripData(
        destination = FutLocation(name = "Lecture Theatre 2"),
        timeToDest = 36.0,
        distanceToDest = 3000.0
    ))
}
