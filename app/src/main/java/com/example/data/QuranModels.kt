package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Surah(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "revelationType") val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahListResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<Surah>
)

@JsonClass(generateAdapter = true)
data class Ayah(
    @Json(name = "number") val number: Int,
    @Json(name = "audio") val audio: String?,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int,
    @Json(name = "page") val page: Int
)

@JsonClass(generateAdapter = true)
data class SurahDetail(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "revelationType") val revelationType: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "ayahs") val ayahs: List<Ayah>
)

@JsonClass(generateAdapter = true)
data class SurahDetailResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: SurahDetail
)
