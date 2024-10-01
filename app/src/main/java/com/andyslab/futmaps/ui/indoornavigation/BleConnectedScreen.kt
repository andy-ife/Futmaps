package com.andyslab.futmaps.ui.indoornavigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyslab.futmaps.R
import com.andyslab.futmaps.domain.entities.ProximityBleResult

@Composable
fun BleConnectedScreen(
    modifier: Modifier = Modifier,
    device: ProximityBleResult
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF52299E), Color(0xFF31185f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "You are in:",
            fontFamily = FontFamily(Font(R.font.sourcesans3_regular),),
            color = Color.White,
            fontSize = 16.sp
            )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = device.name,
            fontFamily = FontFamily(Font(R.font.sourcesans3_regular),),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
            )

        Spacer(modifier = Modifier.height(100.dp))

        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "Beacons",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF614399)
            ),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Row (modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)){
                Column {
                    Card (shape = RoundedCornerShape(4.dp)){
                        Box(modifier = Modifier.padding(4.dp),
                            contentAlignment = Alignment.Center){
                            Icon(
                                painter = painterResource(id = R.drawable.round_bluetooth_24),
                                contentDescription = null,
                                tint = Color(0xFF52299E))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = device.name.uppercase(),
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = device.type,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        color = Color.White,
                        fontSize = 16.sp,
                    )

                    Text(
                        text = device.address,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                }
                    Column(modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End) {

                        Text(
                            text = device.proximity.toString(),
                            fontFamily = FontFamily(Font(R.font.sourcesans3_regular),),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                            Card (
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF388E3C)
                                )
                            ){
                                Box(modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 4.dp)){
                                    Text(text = "Connected",
                                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                        }




            }
        }
    }

}

@Composable
@Preview
fun BleConnectedPreview(){
    BleConnectedScreen(
        device = ProximityBleResult(
            "Dean's Office",
            "Bluetooth LE Beacon",
            "33:44:6A:FF:00:2B",
            "25m"
        ))
}