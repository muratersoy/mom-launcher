package com.momlauncher.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.momlauncher.R
import com.momlauncher.data.PhotoItem
import com.momlauncher.ui.components.BottomHomeButton
import com.momlauncher.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    photos: List<PhotoItem>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    val gridState = rememberLazyGridState()
    val pagerState = rememberPagerState(pageCount = { photos.size })

    // Büyütülmüş fotoğraftayken cihazın geri tuşuna basılırsa önce dikey listeye dönsün
    if (selectedPhotoIndex != null) {
        BackHandler {
            coroutineScope.launch {
                gridState.scrollToItem(pagerState.currentPage)
            }
            selectedPhotoIndex = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        when {
            !hasPermission -> {
                // İzin İsteme Ekranı
                GalleryTopBar(title = stringResource(R.string.gallery_button))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Fotoğrafları görebilmek için telefonunuzdan izin verilmesi gerekiyor.",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryYellow)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRequestPermission
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FOTOĞRAF İZNİ VER",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
                BottomHomeButton(onBack = onBack)
            }

            photos.isEmpty() -> {
                // Fotoğraf Bulunamadı Ekranı
                GalleryTopBar(title = stringResource(R.string.gallery_button))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz fotoğraf bulunamadı",
                        fontSize = 24.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
                BottomHomeButton(onBack = onBack)
            }

            selectedPhotoIndex != null -> {
                // ============================================================
                // BÜYÜTÜLMÜŞ FOTOĞRAF GÖRÜNÜMÜ (Sağ / Sol Kaydırarak Geçiş)
                // ============================================================
                // Üst Bar: Listeye Dön & Sayaç & Ana Sayfa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Listeye Dön Butonu
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceElevated)
                            .border(2.dp, BorderGray, RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    coroutineScope.launch {
                                        gridState.scrollToItem(pagerState.currentPage)
                                    }
                                    selectedPhotoIndex = null
                                }
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Fotoğraf Listesi",
                                tint = TurkishNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LİSTE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    // Fotoğraf Sayacı
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.5.dp, PrimaryYellow, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photos.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryYellow
                        )
                    }
                }

                // Tam Ekran Kaydırılabilir Fotoğraf (HorizontalPager)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF101010))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val photo = photos[page]
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Alt Gezinme Butonları: ÖNCEKİ | LİSTEYE DÖN | SONRAKİ
                val canGoPrev = pagerState.currentPage > 0
                val canGoNext = pagerState.currentPage < photos.size - 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Önceki Fotoğraf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (canGoPrev) DarkSurfaceElevated else DarkSurface)
                            .border(
                                2.dp,
                                if (canGoPrev) BorderGray else BorderGray.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                enabled = canGoPrev,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (canGoPrev) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Önceki",
                                tint = if (canGoPrev) TextWhite else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ÖNCEKİ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canGoPrev) TextWhite else TextMuted
                            )
                        }
                    }

                    // Listeye Dön
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated)
                            .border(2.dp, BorderGray, RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    coroutineScope.launch {
                                        gridState.scrollToItem(pagerState.currentPage)
                                    }
                                    selectedPhotoIndex = null
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = TurkishNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LİSTE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    // Sonraki Fotoğraf
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (canGoNext) PrimaryYellow else DarkSurfaceElevated)
                            .border(
                                2.dp,
                                if (canGoNext) PrimaryYellow else BorderGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                enabled = canGoNext,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (canGoNext) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SONRAKİ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canGoNext) Color.White else TextMuted
                            )
                            if (canGoNext) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Sonraki",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Sabit Alt Ana Sayfa Butonu - Bütün ekranlarda yeri ve boyutu birebir aynı!
                BottomHomeButton(onBack = onBack)
            }

            else -> {
                // ============================================================
                // DİKEY SCROLL EDİLEBİLEN FOTOĞRAF GALERİSİ
                // ============================================================
                GalleryTopBar(title = stringResource(R.string.gallery_button))

                // Dikey Kaydırılabilir 2 Sütunlu Izgara
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(photos) { index, photo ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurfaceVariant)
                                .border(2.dp, BorderGray, RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(index)
                                        }
                                        selectedPhotoIndex = index
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Altta Sabit Dev "ANA SAYFA" Butonu
                BottomHomeButton(onBack = onBack)
            }
        }
    }
}

@Composable
private fun GalleryTopBar(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
    }
}
