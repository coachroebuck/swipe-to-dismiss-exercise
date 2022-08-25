package com.coachroebuck.mytranslationnotes

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

interface MainViewModel {
    sealed class Intent {
        object GetTranslations : Intent()
        data class NewTranslation(val from: String, val to: String) : Intent()
        data class NewGroup(val title: String) : Intent()
        data class NewGroupPending(val title: String) : Intent()
        object SaveNewGroup : Intent()
        object RequestNewGroup : Intent()
        object RequestNewTranslation : Intent()
        data class NewTranslationFromPending(val text: String) : Intent()
        object ProvidedTranslationFrom : Intent()
        data class NewTranslationToPending(val text: String) : Intent()
        object SaveNewTranslation : Intent()
        data class DeleteGroupAtPosition(val position: Int) : Intent()
        data class DeleteTranslationAtPosition(val position: Int) : Intent()
    }

    sealed class ViewState {
        object ShowTranslationList : ViewState()
        object RequestNewGroup : ViewState()
        object RequestNewTranslation : ViewState()

        companion object {
            fun from(last: ViewState) = when (last) {
                is ShowTranslationList -> 0
                is RequestNewGroup -> 1
                is RequestNewTranslation -> 2
            }

            fun to(last: Int) = when (last) {
                0 -> ShowTranslationList
                1 -> RequestNewGroup
                2 -> RequestNewTranslation
                else -> ShowTranslationList
            }
        }
    }

//    val viewState: MutableState<ViewState>

    suspend fun emit(intent: Intent)
    fun onCreate(savedInstanceState: Bundle? = null, callback: (() -> Unit)? = null)
    fun onSaveInstanceState(outState: Bundle, callback: (Bundle) -> Unit)
    val viewState: MutableState<ViewState>
    val currentGroupTitle: MutableState<String>
    val currentTranslationFrom: MutableState<String>
    val currentTranslationTo: MutableState<String>
    val currentTranslations: MutableState<List<TranslationModel>>
    val groupNameTitleRequester: FocusRequester
    val translationFromRequester: FocusRequester
    val translationToRequester: FocusRequester
}

class DefaultMainViewModel(
    private val coroutineScope: CoroutineScope,
    private val getTranslationsMainInteractor: MainInteractor<Unit?>,
    private val addGroupMainInteractor: MainInteractor<String>,
    private val removeGroupMainInteractor: MainInteractor<Int>,
    private val addTranslationMainInteractor: MainInteractor<Pair<String, String>>,
    private val removeTranslationMainInteractor: MainInteractor<Int>,
    override val viewState: MutableState<MainViewModel.ViewState>,
    override val currentGroupTitle: MutableState<String>,
    override val currentTranslationFrom: MutableState<String>,
    override val currentTranslationTo: MutableState<String>,
    override val currentTranslations: MutableState<List<TranslationModel>>,
    override val groupNameTitleRequester: FocusRequester,
    override val translationFromRequester: FocusRequester,
    override val translationToRequester: FocusRequester,
    savedInstanceState: Bundle? = null,
    initialValue: MainViewModel.ViewState? = null
) : MainViewModel {

    override fun onCreate(savedInstanceState: Bundle?, callback: (() -> Unit)?) {
        println("MROEBUCK onCreate(): entering... savedInstanceState=[${savedInstanceState?.keySet()}]")
        createGetTranslationsMainInteractor()
        createAddGroupInteractor()
        createRemoveGroupInteractor()
        createAddTranslationInteractor()
        createRemoveTranslationInteractor()
        savedInstanceState?.let {
            it.getString(LATEST_GROUP_TITLE)?.let { value ->
                currentGroupTitle.value = value
            }
            it.getString(LATEST_ORIGINAL_TEXT)?.let { value ->
                currentTranslationFrom.value = value
            }
            it.getString(LATEST_TRANSLATION_TEXT)?.let { value ->
                currentTranslationTo.value = value
            }
            it.getParcelableArray(LATEST_TRANSLATIONS)?.let { value ->
                currentTranslations.value = value.toList() as List<TranslationModel>
            }
            send(MainViewModel.ViewState.to(it.getInt(VIEW_STATE_VALUE)))
        } ?: run {
            coroutineScope.launch { getTranslationsMainInteractor.interact(Unit) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, callback: (Bundle) -> Unit) {
        outState.putInt(VIEW_STATE_VALUE, MainViewModel.ViewState.from(viewState.value))
        outState.putString(LATEST_GROUP_TITLE, currentGroupTitle.value)
        outState.putString(LATEST_ORIGINAL_TEXT, currentTranslationFrom.value)
        outState.putString(LATEST_TRANSLATION_TEXT, currentTranslationTo.value)
        outState.putParcelableArray(LATEST_TRANSLATIONS, currentTranslations.value.toTypedArray())
    }

    override suspend fun emit(intent: MainViewModel.Intent) {
        coroutineScope.launch {
            when (intent) {
                MainViewModel.Intent.GetTranslations -> onGetTranslations()
                is MainViewModel.Intent.NewTranslation -> onNewTranslation(intent.from, intent.to)
                is MainViewModel.Intent.NewGroup -> onNewGroup(intent.title)
                is MainViewModel.Intent.RequestNewGroup -> onRequestNewGroup()
                is MainViewModel.Intent.RequestNewTranslation -> onRequestNewTranslation()
                is MainViewModel.Intent.NewGroupPending -> onNewGroupPending(intent.title)
                MainViewModel.Intent.SaveNewGroup -> onSaveNewGroup()
                is MainViewModel.Intent.NewTranslationFromPending -> onNewTranslationFromPending(
                    intent.text
                )
                MainViewModel.Intent.ProvidedTranslationFrom -> onProvidedTranslationFrom()
                is MainViewModel.Intent.NewTranslationToPending -> onNewTranslationToPending(intent.text)
                MainViewModel.Intent.SaveNewTranslation -> onSaveNewTranslation()
                is MainViewModel.Intent.DeleteGroupAtPosition -> onDeleteGroupAtPosition(intent.position)
                is MainViewModel.Intent.DeleteTranslationAtPosition -> TODO()
            }
        }
    }

    private fun createGetTranslationsMainInteractor() {
        coroutineScope.launch {
            getTranslationsMainInteractor.responseFlow.collect { translations ->
                this@DefaultMainViewModel.currentTranslations.value = translations
                send(MainViewModel.ViewState.ShowTranslationList)
            }
        }
        coroutineScope.launch {
            getTranslationsMainInteractor.errorFlow.collect { throwable ->
                // TODO: Implement...
            }
        }
    }

    private fun createAddGroupInteractor() {
        coroutineScope.launch {
            addGroupMainInteractor.responseFlow.collect { translations ->
                this@DefaultMainViewModel.currentTranslations.value = translations
                currentGroupTitle.value = ""
            }
        }
        coroutineScope.launch {
            addGroupMainInteractor.errorFlow.collect { throwable ->
                // TODO: Implement...
            }
        }
    }

    private fun createRemoveGroupInteractor() {
        coroutineScope.launch {
            removeGroupMainInteractor.responseFlow.collect { translations ->
//                this@DefaultMainViewModel.translations.value = translations
//                send(MainViewModel.ViewState.ShowTranslationList)
            }
        }
        coroutineScope.launch {
            removeGroupMainInteractor.errorFlow.collect { throwable ->
                // TODO: Implement...
            }
        }
    }

    private fun createAddTranslationInteractor() {
        coroutineScope.launch {
            addTranslationMainInteractor.responseFlow.collect { translations ->
                this@DefaultMainViewModel.currentTranslations.value = translations
                currentTranslationFrom.value = ""
                currentTranslationTo.value = ""
                translationFromRequester.requestFocus()
            }
        }
        coroutineScope.launch {
            addTranslationMainInteractor.errorFlow.collect { throwable ->
                // TODO: Implement...
            }
        }
    }

    private fun createRemoveTranslationInteractor() {
        coroutineScope.launch {
            removeTranslationMainInteractor.responseFlow.collect { translations ->
//                this@DefaultMainViewModel.translations.value = translations
            }
        }
        coroutineScope.launch {
            removeTranslationMainInteractor.errorFlow.collect { throwable ->
                // TODO: Implement...
            }
        }
    }

    private suspend fun onGetTranslations() {
        send(MainViewModel.ViewState.ShowTranslationList)
    }

    private suspend fun onDeleteGroupAtPosition(position: Int) {
        removeGroupMainInteractor.interact(position)
    }

    private fun onNewTranslationFromPending(text: String) {
        currentTranslationFrom.value = text
    }

    private fun onProvidedTranslationFrom() {
        translationToRequester.requestFocus()
    }

    private fun onNewTranslationToPending(text: String) {
        currentTranslationTo.value = text
    }

    private suspend fun onSaveNewTranslation() {
        addTranslationMainInteractor.interact(
            Pair(
                currentTranslationFrom.value,
            currentTranslationTo.value
            )
        )
    }

    private fun onNewGroupPending(title: String) {
        currentGroupTitle.value = title
    }

    private suspend fun onSaveNewGroup() {
        addGroupMainInteractor.interact(currentGroupTitle.value)
    }

    private suspend fun onNewTranslation(from: String, to: String) {
        addTranslationMainInteractor.interact(Pair(from, to))
    }

    private suspend fun onNewGroup(title: String) {
        addGroupMainInteractor.interact(title)
    }

    private fun send(state: MainViewModel.ViewState) {
        coroutineScope.launch {
            viewState.value = state
        }
    }

    private fun onRequestNewGroup() {
        send(MainViewModel.ViewState.RequestNewGroup)
    }

    private fun onRequestNewTranslation() {
        send(MainViewModel.ViewState.RequestNewTranslation)
    }

    private fun isUserDoneEditing(actionId: Int, keyEvent: KeyEvent?) =
        (actionId == EditorInfo.IME_ACTION_NEXT
                || (keyEvent?.action == KeyEvent.ACTION_UP
                && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER))

    companion object {
        private const val VIEW_STATE_VALUE = "VIEW_STATE_VALUE"
        private const val LATEST_GROUP_TITLE = "LATEST_GROUP_TITLE"
        private const val LATEST_ORIGINAL_TEXT = "LATEST_ORIGINAL_TEXT"
        private const val LATEST_TRANSLATION_TEXT = "LATEST_TRANSLATION_TEXT"
        private const val LATEST_TRANSLATIONS = "LATEST_TRANSLATIONS"
    }
}