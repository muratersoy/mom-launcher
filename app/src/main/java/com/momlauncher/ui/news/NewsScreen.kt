package com.momlauncher.ui.news

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.momlauncher.data.NewsItem
import com.momlauncher.ui.components.BottomHomeButton
import com.momlauncher.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsScreen(
    news: List<NewsItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { news.size })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Üst Bar: Başlık, Sayfa Göstergesi ve Yenile Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.news_screen_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                if (news.isNotEmpty()) {
                    Text(
                        text = "Haber ${pagerState.currentPage + 1} / ${news.size}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                }
            }

            // Yenile Butonu
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(2.dp, BorderGray, RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRefresh
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = PrimaryYellow,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.news_refresh),
                        tint = PrimaryYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Orta İçerik Alanı
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading && news.isEmpty() -> {
                    // İlk yükleme durumu
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryYellow,
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.news_loading),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                news.isEmpty() -> {
                    // Boş durum
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_news),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryYellow)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRefresh
                                )
                                .padding(horizontal = 28.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "TEKRAR DENE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                else -> {
                    // Yatay Kaydırılabilir Haber Kartları
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val item = news.getOrNull(pageIndex)
                        if (item != null) {
                            NewsArticleContent(
                                item = item,
                                hasNext = pageIndex < news.size - 1,
                                hasPrev = pageIndex > 0,
                                onNext = {
                                    if (pageIndex < news.size - 1) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pageIndex + 1)
                                        }
                                    }
                                },
                                onPrev = {
                                    if (pageIndex > 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pageIndex - 1)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Haber Değiştirme Butonları (ÖNCEKİ / SONRAKİ)
        if (news.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Önceki Haber Butonu
                val canGoPrev = pagerState.currentPage > 0
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

                // Sonraki Haber Butonu (Büyük ve Sarı - En Kolay Basılan)
                val canGoNext = pagerState.currentPage < news.size - 1
                Box(
                    modifier = Modifier
                        .weight(1.3f)
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
                            text = if (canGoNext) "SONRAKİ" else "SON HABER",
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
        }

        // Sabit Alt Ana Sayfa Butonu - Bütün ekranlarda yeri ve boyutu birebir aynı!
        BottomHomeButton(onBack = onBack)
    }
}

@Composable
private fun NewsArticleContent(
    item: NewsItem,
    hasNext: Boolean,
    hasPrev: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Kaynak ve Tarih Bilgisi
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.source,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow
                )
            }

            if (!item.pubDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.pubDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Büyük Başlık
        Text(
            text = item.title,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Haber Görseli (Varsa)
        if (!item.imageUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceVariant)
                    .border(2.dp, BorderGray, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Reklamsız Haber İçeriği / Özeti
        if (item.description.isNotBlank()) {
            Text(
                text = item.description,
                fontSize = 21.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Normal,
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Haber Sonu "BİR SONRAKİ HABERE GEÇ" Butonu
        if (hasNext) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryYellow)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNext
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SONRAKİ HABERE GEÇ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            // Son habere gelindiyse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceElevated)
                    .border(2.dp, BorderGray, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BÜTÜN GÜNCEL HABERLERİ OKUDUNUZ",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
