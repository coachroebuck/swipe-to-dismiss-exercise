package com.coachroebuck.mytranslationnotes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed class TranslationModel : Parcelable {
    @Parcelize
    @Serializable
    data class TextTranslationGroup(
        val title: String,
        val translations: List<TextTranslationModel> = listOf()
    ) : TranslationModel()

    @Serializable
    @Parcelize
    data class TextTranslationModel(
        val from: String,
        val to: String
    ) : TranslationModel()
}
