package com.coachroebuck.mytranslationnotes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MainRepository {
    val responseFlow: SharedFlow<List<TranslationModel>>
    val errorFlow: SharedFlow<Throwable>
    fun onNewTranslation(from: String, to: String)
    fun onNewGroup(title: String)
    fun onDeleteGroupAtPosition(position: Int)
    fun onDeleteTranslationAtPosition(value: Int)
    suspend fun onGetTranslations()
}

class DefaultMainRepository(
    private val dataStore: DataStore<Preferences>,
    private val translationsCounter: Preferences.Key<String>,
    private val coroutineScope: CoroutineScope,
) : MainRepository {
    private val json = Json {
        encodeDefaults = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        allowStructuredMapKeys = true
        prettyPrint = true
        useArrayPolymorphism = false
        ignoreUnknownKeys = true
    }

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow
    private var translationsCounterFlow: Flow<String> = dataStore.data.map { preferences ->
        // No type safety.
        preferences[translationsCounter] ?: "[]"
    }

    init {
        coroutineScope.launch {
            translationsCounterFlow.collect {
                val data: List<TranslationModel> = json.decodeFromString(it)
                _responseFlow.emit(data)
            }
        }
    }

    override fun onNewTranslation(from: String, to: String) {
       add(TranslationModel.TextTranslationModel(from, to))
    }

    override fun onNewGroup(title: String) {
        add(TranslationModel.TextTranslationGroup(title))
    }

    override fun onDeleteGroupAtPosition(position: Int) {
        delete(position)
    }

    override fun onDeleteTranslationAtPosition(value: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun onGetTranslations() {
        coroutineScope.launch {
            try {
                dataStore.edit { settings : MutablePreferences->
                    val originalText = settings[translationsCounter]
                    /*val data: MutableList<TranslationModel> =
                        if (originalText?.isNotEmpty() == true) {
                            json.decodeFromString<List<TranslationModel>>(originalText)
                                .toMutableList()
                        } else {
                            mutableListOf()
                        }
                    val updatedText = json.encodeToString(data)*/
                    originalText?.let { settings[translationsCounter] = it }
                }
            } catch (t: Throwable) {
                _errorFlow.emit(t)
            }
        }
    }

    private fun delete(position: Int) {
        coroutineScope.launch {
            try {
                dataStore.edit { settings : MutablePreferences->
                    val originalText = settings[translationsCounter]
                    val data: MutableList<TranslationModel> =
                        if (originalText?.isNotEmpty() == true) {
                            json.decodeFromString<List<TranslationModel>>(originalText)
                                .toMutableList()
                        } else {
                            mutableListOf()
                        }
                    data.removeAt(position)
                    val updatedText = json.encodeToString(data)
                    settings[translationsCounter] = updatedText
                }
            } catch (t: Throwable) {
                _errorFlow.emit(t)
            }
        }
    }

    private fun add(newTranslation: TranslationModel) {
        coroutineScope.launch {
            try {
                dataStore.edit { settings ->
                    val originalText = settings[translationsCounter]
                    val data: MutableList<TranslationModel> =
                        if (originalText?.isNotEmpty() == true) {
                            json.decodeFromString<List<TranslationModel>>(originalText)
                                .toMutableList()
                        } else {
                            mutableListOf()
                        }
                    data.add(newTranslation)
                    val updatedText = json.encodeToString(data)
                    settings[translationsCounter] = updatedText
                }
            } catch (t: Throwable) {
                _errorFlow.emit(t)
            }
        }
    }
}
