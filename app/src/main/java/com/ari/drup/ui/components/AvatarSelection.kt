package com.ari.drup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ari.drup.R

@Composable
fun AvatarSelector(onSelectAvatar : (Int) -> Unit,
                   modifier: Modifier = Modifier
) {
    var selection by remember { mutableIntStateOf(0) }
    val avatars = listOf(R.drawable.avt_1,R.drawable.avt_2,R.drawable.avt_4,R.drawable.avt_5,R.drawable.avt_6,R.drawable.avt_7)

    LazyHorizontalGrid(rows = GridCells.Fixed(2),
        modifier= modifier.height(200.dp),
        userScrollEnabled = false,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        ) {
        items(avatars.count()) { idx ->
            Image(
                painterResource(avatars[idx]),
                contentDescription = null,
                modifier = Modifier.clip(CircleShape).clickable{
                    selection = idx
                    onSelectAvatar(idx)
                }.border(if (selection==idx) 3.dp else 0.dp, Color.Red, shape = CircleShape),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview
@Composable
private fun AvatarSelectorPrev() {
    AvatarSelector({})
}