package com.andyslab.futmaps.ui.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.ui.ErrorDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.ui.home.clickableNoRipple
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavHostController, ){
    val viewModel = viewModel<SearchViewModel>()
    val focusRequester = remember{
        FocusRequester()
    }
    var searchQuery by remember{
        mutableStateOf("")
    }

    var justLaunched = remember{true}

    val suggestions by viewModel.suggestions.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    var clearKeyboardFocus by remember{
        mutableStateOf(false)
    }

    var locationNotFound by remember{
        mutableStateOf(false)
    }

    var showAllMatchesText by remember{
        mutableStateOf(false)
    }

    if(clearKeyboardFocus) LocalFocusManager.current.clearFocus()

    LaunchedEffect(key1 = true) {
        if(justLaunched){
            delay(200)
            focusRequester.requestFocus()
            viewModel.onSearchQueryChange(searchQuery){}
            justLaunched = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(vertical = 20.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.Top,
        ){
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Where to?",
            modifier = Modifier.padding(4.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily(Font(R.font.sourcesans3_regular))
            )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                clearKeyboardFocus = false
                showAllMatchesText = false
                locationNotFound = false
                searchQuery = it
                viewModel.onSearchQueryChange(it){
                    if(suggestions.isEmpty() && searchQuery.length>1) locationNotFound = true
                }
                            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
            placeholder = {
                Text(text = "Search faculty, LTs, hostels...",
                    fontFamily = FontFamily(Font(R.font.opensans_regular)))},

            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "search icon",
                    modifier = Modifier.size(30.dp))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions {
                clearKeyboardFocus = true
                viewModel.onSearchButtonClick(searchQuery){
                    if(suggestions.isEmpty()) locationNotFound = true else showAllMatchesText = true
                }
                                              },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFF202020),
                unfocusedContainerColor = Color.White,
                unfocusedPlaceholderColor = Color.Transparent,
                unfocusedLeadingIconColor = Color(0xFF404040),
                unfocusedIndicatorColor = Color(0xFF672976),
                focusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                focusedPlaceholderColor = Color.Transparent,
                focusedLeadingIconColor = Color(0xFF404040),
                focusedIndicatorColor = Color(0xFF672976),
                cursorColor = Color(0xFFC8A4D1)
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        if(searchState is Resource.Loading && searchQuery.isNotBlank()){
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f), Alignment.Center){
                CircularProgressIndicator(color = Color(0xFFC8A4D1))
            }
        }
        else if(searchState is Resource.Error){
            ErrorDialog(message = "Connection error. Please check your internet connection.",
                onDismiss = {viewModel.dismissDialog()}) {
                viewModel.onSearchQueryChange(searchQuery,){
                    if(suggestions.isEmpty() && searchQuery.length>1) locationNotFound = true
                }
            }
        }
        else if(locationNotFound){
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Location not found. Try rephrasing your search or explore a new location.",
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
                )
        }
        else{
            if(showAllMatchesText){
                Text(
                    text = "All matches:",
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.sourcesans3_extralight_italic)),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF606060)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(suggestions.size){
                val item = suggestions.elementAt(it)
                SearchSuggestionItem(
                    name = item.name,
                    desc = item.tag,
                    icon = item.icon
                ){
                    //focusRequester.freeFocus()
                    clearKeyboardFocus = true
                    viewModel.retrieveFutLocation(item.name){
                        navController.navigate(Screen.ShowRouteScreen.route){
                            popUpTo(Screen.SearchScreen.route){
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }}
    }
}

@Composable
fun SearchSuggestionItem(
    modifier: Modifier = Modifier,
    name: String,
    desc: String,
    @DrawableRes icon: Int = R.drawable.generic_office,
    goToSearchResultScreen: () -> Unit
){
Column(
    modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(Color.White),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.Start){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 10.dp)
            .clickableNoRipple(interactionSource = remember{ MutableInteractionSource() }) {
                goToSearchResultScreen()
            },
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center){

            Icon(
                painter = painterResource(id = icon),
                contentDescription = desc,
                modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center) {
            Text(
                text = name,
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold)

            //Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                fontFamily = FontFamily(Font(R.font.sourcesans3_italic),),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider(color = Color(0xFFE3CFE8))

}
}

@Preview
@Composable
fun SearchScreenPreview(){
    SearchScreen(rememberNavController())
//    SearchSuggestionItem(
//        name = "School of Engineering",
//        desc = "SEET and SIPET",
//        icon = R.drawable.generic_office)
}

