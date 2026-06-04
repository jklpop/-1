package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// Reciter Definition
data class Reciter(
    val name: String,
    val id: String,
    val description: String
)

sealed interface SurahListState {
    object Loading : SurahListState
    data class Success(val surahs: List<Surah>) : SurahListState
    data class Error(val message: String) : SurahListState
}

sealed interface SurahDetailState {
    object Idle : SurahDetailState
    object Loading : SurahDetailState
    data class Success(val detail: SurahDetail) : SurahDetailState
    data class Error(val message: String) : SurahDetailState
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("QuranAppPrefs", Context.MODE_PRIVATE)

    // Reciters List
    val reciters = listOf(
        Reciter("عبد الرحمن مسعود", "ar.hudhaify", "تلاوة هادئة ومؤثرة"),
        Reciter("مشاري العفاسي", "ar.alafasy", "صوت عذب وتلاوة متقنة"),
        Reciter("أحمد البلوشي", "ar.ahmedajamy", "تلاوة خاشعة مميزة")
    )

    // Active Reciter - default: Abdul Rahman Massad (re-mapped to high quality ar.hudhaify)
    var selectedReciter by mutableStateOf(reciters[0])
        private set

    // Current Screen state
    var currentScreen by mutableStateOf<QuranScreen>(QuranScreen.Home)
        private set

    // Search Query
    var searchQuery by mutableStateOf("")

    // States
    private val _surahListState = MutableStateFlow<SurahListState>(SurahListState.Loading)
    val surahListState: StateFlow<SurahListState> = _surahListState

    private val _surahDetailState = MutableStateFlow<SurahDetailState>(SurahDetailState.Idle)
    val surahDetailState: StateFlow<SurahDetailState> = _surahDetailState

    // Last opened Surah state
    var lastOpenedSurahId by mutableStateOf<Int?>(null)
        private set
    var lastOpenedSurahName by mutableStateOf<String?>(null)
        private set

    // Audio Playback
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(false)
        private set
    var activeAyahIndex by mutableStateOf<Int?>(null) // index in the surah's ayahs list
        private set

    // Active Surah running in player (so you can play audio when reading a different surah, or see active player banner)
    var activePlayerSurah by mutableStateOf<SurahDetail?>(null)
        private set

    init {
        loadLastOpenedSurah()
        fetchSurahList()
    }

    fun fetchSurahList() {
        viewModelScope.launch {
            _surahListState.value = SurahListState.Loading
            try {
                val response = QuranApiClient.service.getSurahList()
                if (response.code == 200) {
                    _surahListState.value = SurahListState.Success(response.data)
                } else {
                    _surahListState.value = SurahListState.Error("فشلت عملية جلب البيانات من الخادم")
                }
            } catch (e: Exception) {
                _surahListState.value = SurahListState.Error("خطأ في الاتصال بالشبكة. يرجى التحقق من اتصالك بالإنترنت")
                showToast("تحديث البيانات فشل. يرجى التأكد من اتصال الإنترنت")
            }
        }
    }

    fun selectSurah(surahNumber: Int, surahName: String) {
        currentScreen = QuranScreen.Detail(surahNumber, surahName)
        saveLastOpenedSurah(surahNumber, surahName)
        fetchSurahDetail(surahNumber)
    }

    fun fetchSurahDetail(surahNumber: Int) {
        viewModelScope.launch {
            _surahDetailState.value = SurahDetailState.Loading
            try {
                val response = QuranApiClient.service.getSurahDetail(surahNumber, selectedReciter.id)
                if (response.code == 200) {
                    _surahDetailState.value = SurahDetailState.Success(response.data)
                } else {
                    _surahDetailState.value = SurahDetailState.Error("عذرًا، حدث خطأ في جلب تفاصيل السورة")
                }
            } catch (e: Exception) {
                _surahDetailState.value = SurahDetailState.Error("فشل تحميل السورة. يرجى التحقق من اتصال الإنترنت")
                showToast("تعذر الاتصال بالخادم لتحميل السورة")
            }
        }
    }

    fun changeReciter(reciter: Reciter) {
        if (selectedReciter.id != reciter.id) {
            selectedReciter = reciter
            showToast("تم تغيير القارئ إلى: ${reciter.name}")
            
            // If viewing detail, reload detail for the active surah to fetch the new reciter's audio URLs
            val screen = currentScreen
            if (screen is QuranScreen.Detail) {
                fetchSurahDetail(screen.surahNumber)
            }
            
            // Stop current player if active to prevent misalignment
            stopAudio()
        }
    }

    fun goBackToHome() {
        currentScreen = QuranScreen.Home
    }

    // Audio Controls
    fun playAyah(surah: SurahDetail, index: Int) {
        val ayahs = surah.ayahs
        if (index < 0 || index >= ayahs.size) {
            stopAudio()
            return
        }

        val ayah = ayahs[index]
        val audioUrl = ayah.audio

        if (audioUrl.isNullOrEmpty()) {
            showToast("التلاوة الصوتية غير متوفرة لهذه الآية")
            return
        }

        stopAudioOnly()
        activePlayerSurah = surah
        activeAyahIndex = index
        isBuffering = true
        isPlaying = true

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener {
                    this@QuranViewModel.isBuffering = false
                    start()
                }
                setOnCompletionListener {
                    // Auto-advance to next Ayah when complete
                    if (index + 1 < ayahs.size) {
                        playAyah(surah, index + 1)
                    } else {
                        stopAudio()
                        showToast("اكتملت تلاوة السورة المباركة")
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuranAudio", "Error occurred during playback: what=$what, extra=$extra")
                    this@QuranViewModel.isBuffering = false
                    this@QuranViewModel.isPlaying = false
                    showToast("عذرًا، حدث خطأ في بث الصوت")
                    false
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            this@QuranViewModel.isBuffering = false
            this@QuranViewModel.isPlaying = false
            showToast("خطأ أثناء إعداد مشغل الصوت")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (isPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.start()
            isPlaying = true
        }
    }

    fun stopAudio() {
        stopAudioOnly()
        activeAyahIndex = null
        activePlayerSurah = null
    }

    private fun stopAudioOnly() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("QuranAudio", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            isPlaying = false
            isBuffering = false
        }
    }

    fun playPrevious() {
        val surah = activePlayerSurah ?: return
        val index = activeAyahIndex ?: return
        if (index > 0) {
            playAyah(surah, index - 1)
        }
    }

    fun playNext() {
        val surah = activePlayerSurah ?: return
        val index = activeAyahIndex ?: return
        if (index + 1 < surah.ayahs.size) {
            playAyah(surah, index + 1)
        }
    }

    // LocalStorage / SharedPreferences persistence
    private fun saveLastOpenedSurah(id: Int, name: String) {
        sharedPrefs.edit().apply {
            putInt("last_opened_surah_id", id)
            putString("last_opened_surah_name", name)
            apply()
        }
        lastOpenedSurahId = id
        lastOpenedSurahName = name
    }

    private fun loadLastOpenedSurah() {
        val id = sharedPrefs.getInt("last_opened_surah_id", -1)
        val name = sharedPrefs.getString("last_opened_surah_name", null)
        if (id != -1 && name != null) {
            lastOpenedSurahId = id
            lastOpenedSurahName = name
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}

sealed class QuranScreen {
    object Home : QuranScreen()
    data class Detail(val surahNumber: Int, val surahName: String) : QuranScreen()
}
