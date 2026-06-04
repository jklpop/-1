package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.viewmodel.*
import com.example.data.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force fully RTL layout direction for complete Arabic native alignment
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = NavyPrimary
                    ) { innerPadding ->
                        QuranAppContainer(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuranAppContainer(
    modifier: Modifier = Modifier,
    viewModel: QuranViewModel = viewModel()
) {
    val surahListState by viewModel.surahListState.collectAsStateWithLifecycle()
    val surahDetailState by viewModel.surahDetailState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyPrimary, Color(0xFF040713))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Top Branding Header (Only if on Home Screen)
            if (viewModel.currentScreen is QuranScreen.Home) {
                HomeTopBar(viewModel)
            }

            // Screen Content Routing with elegant Crossfade transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = viewModel.currentScreen,
                    animationSpec = tween(durationMillis = 350, easing = EaseInOutQuad),
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is QuranScreen.Home -> {
                            HomeScreenContent(
                                state = surahListState,
                                viewModel = viewModel
                            )
                        }
                        is QuranScreen.Detail -> {
                            SurahDetailScreen(
                                surahNumber = screen.surahNumber,
                                surahName = screen.surahName,
                                state = surahDetailState,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // Persistent Bottom Audio Player with slide up animation
        AnimatedVisibility(
            visible = viewModel.activePlayerSurah != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomAudioPlayerCard(viewModel = viewModel)
        }
    }
}

@Composable
fun HomeTopBar(viewModel: QuranViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Decorative Star Shield
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(68.dp)
        ) {
            // Overlapping rotated squares making custom gorgeous Rub el Hizb logo frame
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .rotate(45f)
                    .border(2.dp, GoldPrimary, RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(2.dp, GoldPrimary, RoundedCornerShape(10.dp))
            )
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Quran Logo",
                tint = GoldPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "القرآن الكريم",
            style = MaterialTheme.typography.displayMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "تِلْكَ آيَاتُ الْكِتَابِ الْحَكِيمِ",
            style = MaterialTheme.typography.labelMedium,
            color = CustomGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeScreenContent(
    state: SurahListState,
    viewModel: QuranViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Horizontal Reciters Selection Pill Bar
        Text(
            text = "اختر القارئ المفضل:",
            style = MaterialTheme.typography.titleMedium,
            color = CreamText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.reciters.size) { index ->
                val reciter = viewModel.reciters[index]
                val isSelected = viewModel.selectedReciter == reciter
                
                Surface(
                    onClick = { viewModel.changeReciter(reciter) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) GoldPrimary else NavyCard,
                    border = BorderStroke(1.dp, if (isSelected) GoldPrimary else NavyAccent),
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                            contentDescription = "Reciter Icon",
                            tint = if (isSelected) NavyPrimary else GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = reciter.name,
                                color = if (isSelected) NavyPrimary else CreamText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Last-Read Resume Header
        if (viewModel.lastOpenedSurahId != null && viewModel.lastOpenedSurahName != null) {
            QuickResumeCard(
                surahId = viewModel.lastOpenedSurahId!!,
                surahName = viewModel.lastOpenedSurahName!!,
                onClick = {
                    viewModel.selectSurah(
                        viewModel.lastOpenedSurahId!!,
                        viewModel.lastOpenedSurahName!!
                    )
                }
            )
        }

        // Interactive Instant Search Text Field
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("ابحث عن السورة...", color = CustomGray) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CreamText,
                unfocusedTextColor = CreamText,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = NavyAccent,
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search icon",
                    tint = GoldPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // Surah List Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                is SurahListState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GoldPrimary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري تحميل السور المباركة...",
                            color = CreamText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                is SurahListState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Error Connection",
                            tint = CustomRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = CreamText,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchSurahList() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("إعادة المحاولة", color = NavyPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is SurahListState.Success -> {
                    val filteredSurahs = state.surahs.filter {
                        it.name.contains(viewModel.searchQuery) ||
                        it.englishName.contains(viewModel.searchQuery, ignoreCase = true)
                    }

                    if (filteredSurahs.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No Surahs Found",
                                tint = CustomGray,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "عذرًا، لا توجد سورة تطابق البحث",
                                color = CustomGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(filteredSurahs) { _, surah ->
                                SurahListItem(surah = surah, onClick = {
                                    viewModel.selectSurah(surah.number, surah.name)
                                })
                            }
                            // Bottom padding so items are not blocked by floating bottom player
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickResumeCard(
    surahId: Int,
    surahName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(GoldPrimary.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdded,
                    contentDescription = "Resume Icon",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مواصلة القراءة",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = surahName,
                    color = CreamText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Resume chevron",
                tint = GoldPrimary
            )
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NavyAccent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rub el Hizb Number Frame
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .rotate(45f)
                        .background(GoldPrimary.copy(alpha = 0.1f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                )
                Text(
                    text = "${surah.number}",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    color = CreamText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (surah.revelationType == "Meccan") "مكية" else "مدنية",
                        color = GoldPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "•",
                        color = CustomGray,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "آياتها: ${surah.numberOfAyahs}",
                        color = CustomGray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Handled beautifully by Arabic RTL
                contentDescription = "Arrow details",
                tint = GoldPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    surahName: String,
    state: SurahDetailState,
    viewModel: QuranViewModel
) {
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Detailed Header Toolbar
        Surface(
            color = NavyCard,
            contentColor = CreamText,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.goBackToHome() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, // RTL handles this beautifully
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surahName,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "القارئ: ${viewModel.selectedReciter.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CustomGray
                    )
                }

                // Play whole surah icon button
                if (state is SurahDetailState.Success) {
                    IconButton(
                        onClick = { viewModel.playAyah(state.detail, 0) },
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.12f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Complete Surah",
                            tint = GoldPrimary
                        )
                    }
                }
            }
        }

        // Surah reading content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                is SurahDetailState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("جاري تنزيل الآيات المباركة...", color = CreamText)
                    }
                }
                is SurahDetailState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error detail",
                            tint = CustomRed,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = CreamText, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.fetchSurahDetail(surahNumber) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("تحديث الآيات", color = NavyPrimary)
                        }
                    }
                }
                is SurahDetailState.Success -> {
                    val detail = state.detail
                    
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Basmala Header Display (Omitting form Surah At-Tawbah, id = 9)
                        if (surahNumber != 9) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            fontFamily = QuranFontFamily,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(180.dp)
                                                .height(1.dp)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color.Transparent, GoldPrimary, Color.Transparent)
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        // Listing all Verses (Ayahs)
                        itemsIndexed(detail.ayahs) { index, ayah ->
                            val isPlayingThisAyah = viewModel.activePlayerSurah?.number == detail.number &&
                                    viewModel.activeAyahIndex == index

                            AyahRowItem(
                                ayah = ayah,
                                isPlaying = isPlayingThisAyah,
                                onClick = {
                                    viewModel.playAyah(detail, index)
                                }
                            )
                        }

                        // Floating bottom player cushion padding
                        item {
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AyahRowItem(
    ayah: Ayah,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) GoldPrimary.copy(alpha = 0.04f) else NavyCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPlaying) GoldPrimary else NavyAccent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            // Verse Action Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Verse identifier tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(GoldPrimary.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Text(
                            text = "${ayah.numberInSurah}",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "جُزْء ${ayah.juz} • صَفْحَة ${ayah.page}",
                        color = CustomGray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Interactive Audio indicator
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = "Play ayah",
                        tint = if (isPlaying) PlayGreen else GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Beautiful Arabic Text block
            Text(
                text = "${ayah.text} ﴿ ${ayah.numberInSurah} ﴾",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) GoldPrimary else CreamText,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun BottomAudioPlayerCard(viewModel: QuranViewModel) {
    val surah = viewModel.activePlayerSurah ?: return
    val activeAyahIndex = viewModel.activeAyahIndex ?: return
    val currentAyah = surah.ayahs.getOrNull(activeAyahIndex) ?: return

    Surface(
        color = NavyCard,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Meta info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "الآية ${currentAyah.numberInSurah}",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "القارئ: ${viewModel.selectedReciter.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CustomGray
                    )
                }

                // buffering loading spinner
                if (viewModel.isBuffering) {
                    CircularProgressIndicator(
                        color = GoldPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player control buttons (RTL directional logic built-in)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Play Previous Ayah Button
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    enabled = activeAyahIndex > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Mirrored correctly in RTL for backward skip
                        contentDescription = "Previous Ayah",
                        tint = if (activeAyahIndex > 0) GoldPrimary else CustomGray.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(GoldPrimary, CircleShape)
                        .clip(CircleShape)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Pause",
                        tint = NavyPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play Next Ayah Button
                IconButton(
                    onClick = { viewModel.playNext() },
                    enabled = activeAyahIndex + 1 < surah.ayahs.size,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Mirrored correctly in RTL for forward skip
                        contentDescription = "Next Ayah",
                        tint = if (activeAyahIndex + 1 < surah.ayahs.size) GoldPrimary else CustomGray.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Stop Button
                IconButton(
                    onClick = { viewModel.stopAudio() },
                    modifier = Modifier
                        .background(CustomRed.copy(alpha = 0.1f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Playback",
                        tint = CustomRed
                    )
                }
            }
            
            // Layout padding for bottom system navigation gesture bar/notch spacing
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
