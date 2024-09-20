package com.andyslab.futmaps.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.ui.Screen

@Composable
fun LoginScreen(navController: NavHostController){
    val viewModel = viewModel<LoginViewModel>()

    var username by remember{
        mutableStateOf("")
    }
    var password by remember{
        mutableStateOf("")
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)){

        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.376f)){

            Image(
                painter = painterResource(id = R.drawable.login_app_bar),
                contentDescription = "app bar",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth)

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),) {
                Spacer(modifier = Modifier.height(110.dp))

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){

                    Text(
                        text = "FUTMaps",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.patrick_hand_regular))
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = "Admin Login",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),

                )

                Text(
                    text = "Log in to add and edit locations on the map",
                    color = Color(0xffFFFFFF),
                    fontSize = 14.sp,
                    //fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily(Font(R.font.sourcesans_3_extra_light)),

                )
            }
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.End) {

            OutlinedTextField(
                value = username,
                onValueChange = {username = it},
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontSize = 16.sp),
                placeholder = {Text(text = "Username",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)))},
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF672976),
                    unfocusedContainerColor = Color(0xFFf7f0f8),
                    unfocusedBorderColor = Color(0xFF404040),
                    unfocusedPlaceholderColor = Color(0xFF404040),
                    focusedPlaceholderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color(0xFFC8A4D1)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontSize = 16.sp),
                placeholder = {Text(text ="Password",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular))
                )},
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF672976),
                    unfocusedContainerColor = Color(0xFFf7f0f8),
                    unfocusedBorderColor = Color(0xFF404040),
                    unfocusedPlaceholderColor = Color(0xFF404040),
                    focusedPlaceholderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color(0xFFC8A4D1)))

            Text(text = "Reset Password",
                modifier = Modifier
                    .clickable(){},
                fontSize = 14.sp,
                color = Color(0xff672976),
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontWeight = FontWeight.SemiBold)

            TextButton(
                onClick = {
                    if(username.trim() == "admin" && password.trim() == "12345678"){
                    UserProfile.instance.isAdmin = true
                    navController.navigate(Screen.HomeScreen.route){
                        popUpTo(Screen.HomeScreen.route){
                            inclusive = true
                        }
                    }
                } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF672976),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Login",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold)
            }

            Text(text = "Logout",
                modifier = Modifier
                    .clickable(){
                        UserProfile.instance.isAdmin = false
                            navController.navigate(Screen.HomeScreen.route){
                                popUpTo(Screen.HomeScreen.route){
                                    inclusive = true
                                }
                            }
                    },
                fontSize = 14.sp,
                color = Color(0xff672976),
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular),),
                fontWeight = FontWeight.SemiBold)

        }

    }
}

@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen(rememberNavController())
}