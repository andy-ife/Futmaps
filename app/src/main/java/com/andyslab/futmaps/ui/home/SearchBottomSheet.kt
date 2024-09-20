package com.andyslab.futmaps.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyslab.futmaps.R
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.QuickSearchItems

@Composable
fun SearchBottomSheet(
    modifier: Modifier = Modifier,
    searchQueryState: MutableState<String> = mutableStateOf(""),
    suggestedSearches: Set<FutLocation> = setOf(),
    goToSearchScreen: () -> Unit = {},
    goToSearchResultScreen: (String) -> Unit = {}
){
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(key1 = isFocused) {
        if(isFocused){
            goToSearchScreen()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)) {

        //the point of this second column is so that I can apply padding
        //to ONLY search bar and the 'quick search' text. The lazy
        //list below should extend to the edges of the screen (no padding)
        Column(modifier = Modifier.padding(horizontal = 10.dp,)){
            Box(modifier = Modifier.wrapContentSize()) {

                TextField(
            value = searchQueryState.value,
            onValueChange = {searchQueryState.value = it},
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.Transparent, RoundedCornerShape(20.dp)),
                enabled = false,
                textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
            ),
                placeholder = {
                Text(text = "Search faculties, halls, hostels...",
                    fontFamily = FontFamily(Font(R.font.opensans_regular)))},

            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "search icon",
                    modifier = Modifier.size(30.dp))},
                singleLine = true,
                interactionSource = interactionSource,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFF202020),
                unfocusedContainerColor = Color(0xFFF4ecf6),
                unfocusedPlaceholderColor = Color(0xFF202020),
                unfocusedLeadingIconColor = Color(0xFF404040),
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                focusedContainerColor = Color(0xFFF4ecf6),
                focusedPlaceholderColor = Color.Transparent,
                focusedLeadingIconColor = Color(0xFF404040),
                focusedIndicatorColor = Color(0xFF672976),
                disabledContainerColor = Color(0xFFF4ecf6),
                disabledPlaceholderColor = Color(0xFF404040),
                disabledLeadingIconColor = Color(0xFF404040),
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFC8A4D1)
            )
        )
            TextButton(//drawn on top of textfield to give it a ripple
                onClick = { goToSearchScreen() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent
                )
            ) {
            }
            }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Search",
            color = Color(0xFF606060),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.sourcesans3_italic))
            )}

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            items(10){
                val item = suggestedSearches.elementAt(it)
                QuickSearchItemButton(
                    item = item,
                    ){
                    goToSearchResultScreen(item.name)
                }
            }
        }

    }
}

@Preview
@Composable
fun SearchBottomSheetPreview(){
    SearchBottomSheet(
        suggestedSearches = QuickSearchItems.items
    )
}

@Composable
fun QuickSearchItemButton(item: FutLocation, onClick: () -> Unit){
    TextButton(
        onClick = {onClick()},
        modifier = Modifier
            .wrapContentWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF672976)
        ),
        elevation = ButtonDefaults.buttonElevation(2.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,){
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = "quick search icon",
            modifier = Modifier.size(24.dp),)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
            text = item.name,
                modifier = Modifier.offset(y = 2.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
        )
        }
    }
}

fun Modifier.clickableNoRipple(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) = run {
    this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {onClick()}
        )
    )
}
