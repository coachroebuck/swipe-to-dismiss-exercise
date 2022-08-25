package com.coachroebuck.mytranslationnotes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

interface MainInteractor<T> {
    val responseFlow: SharedFlow<List<TranslationModel>>
    val errorFlow: SharedFlow<Throwable>
    suspend fun interact(value: T)
}

class AddGroupMainInteractor(
    private val repository: MainRepository,
    coroutineScope: CoroutineScope
) : MainInteractor<String> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    private var isExecuting = false

    init {
        coroutineScope.launch {
            repository.responseFlow.collect { data ->
                if(isExecuting) {
                    _responseFlow.emit(data)
                    isExecuting = false
                }
            }
        }
        coroutineScope.launch {
            repository.errorFlow.collect { throwable ->
                if(isExecuting) {
                    _errorFlow.emit(throwable)
                    isExecuting = false
                }
            }
        }
    }

    override suspend fun interact(value: String) {
        repository.onNewGroup(value)
        isExecuting = true
    }
}

class FakeSuccessAddGroupMainInteractor() : MainInteractor<String> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: String) {
        val data: List<TranslationModel> = listOf(
            TranslationModel.TextTranslationGroup(
                title = "English to Spanish",
                translations = listOf()
            ),
            TranslationModel.TextTranslationGroup(
                title = "English to French",
                translations = listOf()
            ),
            TranslationModel.TextTranslationGroup(
                title = "English to German",
                translations = listOf()
            )
        )
        _responseFlow.emit(data)
    }
}

class FakeErrorAddGroupMainInteractor() : MainInteractor<String> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: String) {
        _errorFlow.emit(Throwable("Fake Error Occurred"))
    }
}

class RemoveGroupMainInteractor(
    private val repository: MainRepository,
    coroutineScope: CoroutineScope
) : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    private var isExecuting = false

    init {
        coroutineScope.launch {
            repository.responseFlow.collect { data ->
                if(isExecuting) {
                    _responseFlow.emit(data)
                    isExecuting = false
                }
            }
        }
        coroutineScope.launch {
            repository.errorFlow.collect { throwable ->
                if(isExecuting) {
                    _errorFlow.emit(throwable)
                    isExecuting = false
                }
            }
        }
    }

    override suspend fun interact(value: Int) {
        repository.onDeleteGroupAtPosition(value)
        isExecuting = true
    }
}

class FakeSuccessRemoveGroupMainInteractor() : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Int) {
        val data: List<TranslationModel> = listOf(
            TranslationModel.TextTranslationGroup(
                title = "English to Spanish",
                translations = listOf()
            ),
            TranslationModel.TextTranslationGroup(
                title = "English to French",
                translations = listOf()
            ),
            TranslationModel.TextTranslationGroup(
                title = "English to German",
                translations = listOf()
            )
        )
        _responseFlow.emit(data)
    }
}

class FakeErrorRemoveGroupMainInteractor() : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Int) {
        _errorFlow.emit(Throwable("Fake Error Occurred"))
    }
}

class AddTranslationMainInteractor(
    private val repository: MainRepository,
    coroutineScope: CoroutineScope
) : MainInteractor<Pair<String, String>> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    private var isExecuting = false

    init {
        coroutineScope.launch {
            repository.responseFlow.collect { data ->
                if(isExecuting) {
                    _responseFlow.emit(data)
                    isExecuting = false
                }
            }
        }
        coroutineScope.launch {
            repository.errorFlow.collect { throwable ->
                if(isExecuting) {
                    _errorFlow.emit(throwable)
                    isExecuting = false
                }
            }
        }
    }

    override suspend fun interact(value: Pair<String, String>) {
        repository.onNewTranslation(value.first, value.second)
        isExecuting = true
    }
}

class FakeSuccessAddTranslationMainInteractor() : MainInteractor<Pair<String, String>> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Pair<String, String>) {
        val data: List<TranslationModel> = listOf(
            TranslationModel.TextTranslationModel(from = "Hi!", to = "Ola"),
            TranslationModel.TextTranslationModel(from = "I am", to = "Yo soy"),
            TranslationModel.TextTranslationModel(from = "You are", to = "Tu erés"),
        )
        _responseFlow.emit(data)
    }
}

class FakeErrorAddTranslationMainInteractor() : MainInteractor<Pair<String, String>> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Pair<String, String>) {
        _errorFlow.emit(Throwable("Fake Error Occurred"))
    }
}

class RemoveTranslationMainInteractor(
    private val repository: MainRepository,
    coroutineScope: CoroutineScope
) : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    private var isExecuting = false

    init {
        coroutineScope.launch {
            repository.responseFlow.collect { data ->
                if(isExecuting) {
                    _responseFlow.emit(data)
                    isExecuting = false
                }
            }
        }
        coroutineScope.launch {
            repository.errorFlow.collect { throwable ->
                if(isExecuting) {
                    _errorFlow.emit(throwable)
                    isExecuting = false
                }
            }
        }
    }

    override suspend fun interact(value: Int) {
        repository.onDeleteTranslationAtPosition(value)
        isExecuting = true
    }
}

class FakeSuccessRemoveTranslationMainInteractor() : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Int) {
        val data: List<TranslationModel> = listOf(
            TranslationModel.TextTranslationModel(from = "Hi!", to = "Ola"),
            TranslationModel.TextTranslationModel(from = "I am", to = "Yo soy"),
            TranslationModel.TextTranslationModel(from = "You are", to = "Tu erés"),
        )
        _responseFlow.emit(data)
    }
}

class FakeErrorRemoveTranslationMainInteractor() : MainInteractor<Int> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Int) {
        _errorFlow.emit(Throwable("Fake Error Occurred"))
    }
}

class GetTranslationsMainInteractor(
    private val repository: MainRepository,
    coroutineScope: CoroutineScope
) : MainInteractor<Unit?> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    private var isExecuting = false

    init {
        coroutineScope.launch {
            repository.responseFlow.collect { data ->
                if(isExecuting) {
                    _responseFlow.emit(data)
                    isExecuting = false
                }
            }
        }
        coroutineScope.launch {
            repository.errorFlow.collect { throwable ->
                if(isExecuting) {
                    _errorFlow.emit(throwable)
                    isExecuting = false
                }
            }
        }
    }

    override suspend fun interact(value: Unit?) {
        isExecuting = true
        repository.onGetTranslations()
    }
}

class FakeSuccessGetTranslationsMainInteractor() : MainInteractor<Unit?> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Unit?) {
        val data: List<TranslationModel> = listOf(
            TranslationModel.TextTranslationModel(from = "Hi!", to = "Ola"),
            TranslationModel.TextTranslationModel(from = "I am", to = "Yo soy"),
            TranslationModel.TextTranslationModel(from = "You are", to = "Tu erés"),
        )
        _responseFlow.emit(data)
    }
}

class FakeErrorGetTranslationsMainInteractor() : MainInteractor<Unit?> {

    private val _responseFlow = MutableSharedFlow<List<TranslationModel>>()
    override val responseFlow: SharedFlow<List<TranslationModel>> = _responseFlow

    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val errorFlow: SharedFlow<Throwable> = _errorFlow

    override suspend fun interact(value: Unit?) {
        _errorFlow.emit(Throwable("Fake Error Occurred"))
    }
}
