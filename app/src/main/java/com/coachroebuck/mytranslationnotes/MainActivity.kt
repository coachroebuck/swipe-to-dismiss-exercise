@file:Suppress("UNUSED_PARAMETER")

package com.coachroebuck.mytranslationnotes

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coachroebuck.mytranslationnotes.TranslationModel.TextTranslationGroup
import com.coachroebuck.mytranslationnotes.ui.theme.MyTranslationNotesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {

    enum class MockType { Real, FakeError, FakeSuccess }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "translations")
    }

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var fabShape: CornerBasedShape
    private lateinit var scaffoldState: ScaffoldState
    private lateinit var viewModel: MainViewModel
    private lateinit var focusManager: FocusManager

    // Consider negative values to mean 'cut corner' and positive values to mean 'round corner'
    private val sharpEdgePercent = -50f
    private val roundEdgePercent = 45f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContentView(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.onSaveInstanceState(outState) {
            super.onSaveInstanceState(it)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    @Composable
    fun ContentView(
        savedInstanceState: Bundle? = null,
        mockType: MockType = MockType.Real,
        initialList: List<TranslationModel> = listOf(),
        viewState: MainViewModel.ViewState = MainViewModel.ViewState.ShowTranslationList
    ) {
        MyTranslationNotesTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                CreateMutableStates()
                when (mockType) {
                    MockType.Real -> CreateRealViewModel(savedInstanceState, initialList, viewState)
                    MockType.FakeError -> CreateFakeErrorViewModel(initialList, viewState)
                    MockType.FakeSuccess -> CreateFakeSuccessViewModel(initialList, viewState)
                }

                CreateScaffold(savedInstanceState)
            }
        }
    }

    @Composable
    private fun CreateMutableStates() {
        scaffoldState = rememberScaffoldState()
        coroutineScope = rememberCoroutineScope()
        focusManager = LocalFocusManager.current
    }

    @Composable
    private fun CreateFakeErrorViewModel(
        initialList: List<TranslationModel>,
        viewState: MainViewModel.ViewState
    ) {
        viewModel = DefaultMainViewModel(
            getTranslationsMainInteractor = FakeErrorGetTranslationsMainInteractor(),
            addGroupMainInteractor = FakeErrorAddGroupMainInteractor(),
            removeGroupMainInteractor = FakeErrorRemoveGroupMainInteractor(),
            addTranslationMainInteractor = FakeErrorAddTranslationMainInteractor(),
            removeTranslationMainInteractor = FakeErrorRemoveTranslationMainInteractor(),
            viewState = remember { mutableStateOf(viewState) },
            currentGroupTitle = remember { mutableStateOf("") },
            currentTranslationFrom = remember { mutableStateOf("") },
            currentTranslationTo = remember { mutableStateOf("") },
            currentTranslations = remember { mutableStateOf(initialList) },
            groupNameTitleRequester = remember { FocusRequester() },
            translationFromRequester = remember { FocusRequester() },
            translationToRequester = remember { FocusRequester() },
            coroutineScope = coroutineScope
        )
    }

    @Composable
    private fun CreateFakeSuccessViewModel(
        initialList: List<TranslationModel>,
        viewState: MainViewModel.ViewState = MainViewModel.ViewState.ShowTranslationList
    ) {
        viewModel = DefaultMainViewModel(
            getTranslationsMainInteractor = FakeSuccessGetTranslationsMainInteractor(),
            addGroupMainInteractor = FakeSuccessAddGroupMainInteractor(),
            removeGroupMainInteractor = FakeSuccessRemoveGroupMainInteractor(),
            addTranslationMainInteractor = FakeSuccessAddTranslationMainInteractor(),
            removeTranslationMainInteractor = FakeSuccessRemoveTranslationMainInteractor(),
            viewState = remember { mutableStateOf(viewState) },
            currentGroupTitle = remember { mutableStateOf("") },
            currentTranslationFrom = remember { mutableStateOf("") },
            currentTranslationTo = remember { mutableStateOf("") },
            currentTranslations = remember { mutableStateOf(initialList) },
            groupNameTitleRequester = remember { FocusRequester() },
            translationFromRequester = remember { FocusRequester() },
            translationToRequester = remember { FocusRequester() },
            coroutineScope = coroutineScope
        )
    }

    @Composable
    private fun CreateRealViewModel(
        savedInstanceState: Bundle?,
        initialList: List<TranslationModel>,
        viewState: MainViewModel.ViewState
    ) {
        val mainRepository = defaultMainRepository()
        viewModel = DefaultMainViewModel(
            getTranslationsMainInteractor = GetTranslationsMainInteractor(
                repository = mainRepository,
                coroutineScope = coroutineScope,
            ),
            addGroupMainInteractor = AddGroupMainInteractor(
                repository = mainRepository,
                coroutineScope = coroutineScope,
            ),
            removeGroupMainInteractor = RemoveGroupMainInteractor(
                repository = mainRepository,
                coroutineScope = coroutineScope,
            ),
            addTranslationMainInteractor = AddTranslationMainInteractor(
                repository = mainRepository,
                coroutineScope = coroutineScope,
            ),
            removeTranslationMainInteractor = RemoveTranslationMainInteractor(
                repository = mainRepository,
                coroutineScope = coroutineScope,
            ),
            savedInstanceState = savedInstanceState,
            viewState = remember { mutableStateOf(viewState) },
            currentGroupTitle = remember { mutableStateOf("") },
            currentTranslationFrom = remember { mutableStateOf("") },
            currentTranslationTo = remember { mutableStateOf("") },
            currentTranslations = remember { mutableStateOf(initialList) },
            groupNameTitleRequester = remember { FocusRequester() },
            translationFromRequester = remember { FocusRequester() },
            translationToRequester = remember { FocusRequester() },
            coroutineScope = coroutineScope
        )
    }

    @Composable
    private fun defaultMainRepository() : MainRepository {
        return DefaultMainRepository(
            dataStore,
            stringPreferencesKey("translations"),
            coroutineScope
        )
    }

    // region Scaffold

    @Composable
    private fun CreateScaffold(savedInstanceState: Bundle? = null,) {
        PreCreateScaffold()
        viewModel.onCreate(savedInstanceState, null)
        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { CreateSnackBarHost(it) },
            topBar = { CreateTopAppBar() },
            bottomBar = { CreateBottomBar() },
            drawerContent = { CreateDrawerContent(this) },
            floatingActionButton = { BuildFloatingActionButton() },
            floatingActionButtonPosition = FabPosition.Center,
            drawerGesturesEnabled = true,
            isFloatingActionButtonDocked = true,
        ) {
            ScaffoldContentView(it)
        }
    }

    @Composable
    private fun PreCreateScaffold() {
        // Start with sharp edges
        val animatedProgress = remember { Animatable(sharpEdgePercent) }
        // animation value to animate shape
        val progress = animatedProgress.value.roundToInt()

        // When progress is 0, there is no modification to the edges
        // so we are just drawing a rectangle.
        // This allows for a smooth transition between cut corners and round corners.
        fabShape = when {
            progress < 0 -> {
                CutCornerShape(abs(progress))
            }
            progress == roundEdgePercent.toInt() -> {
                CircleShape
            }
            else -> {
                RoundedCornerShape(progress)
            }
        }
    }

    @Composable
    private fun CreateDrawerContent(columnScope: ColumnScope) {
        CreateDrawerContentOption(
            R.string.manage_translations,
            MainViewModel.Intent.GetTranslations
        )
        CreateDrawerContentOption(
            R.string.new_group,
            MainViewModel.Intent.RequestNewGroup
        )
        CreateDrawerContentOption(
            R.string.new_translation,
            MainViewModel.Intent.RequestNewTranslation
        )
    }

    @Composable
    private fun CreateDrawerContentOption(
        resourceId: Int,
        intent: MainViewModel.Intent,
    ) {
        Column(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        viewModel.emit(intent)
                        scaffoldState.drawerState.close()
                    }
                },
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = resourceId)
            )
        }
        Divider()
    }

    @Composable
    private fun CreateTopAppBar() {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            navigationIcon = { },
            actions = { }
        )
    }

    @Composable
    private fun CreateBottomBar() {
        val isExpanded = rememberSaveable { mutableStateOf(false) }

        BottomAppBar(cutoutShape = fabShape) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        isExpanded.value = true
                        scaffoldState.drawerState.open()
                    }
                }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    }

    @Composable
    private fun CreateSnackBarHost(snackBarHostState: SnackbarHostState) {
        // reuse default SnackBarHost to have default animation and timing handling
        SnackbarHost(snackBarHostState) { data ->
            // custom snackBar with the custom border
            Snackbar(
                modifier = Modifier,
                snackbarData = data
            )
        }
    }

    @Composable
    fun BuildFloatingActionButton() {
        ExtendedFloatingActionButton(
            modifier = Modifier,
            elevation = FloatingActionButtonDefaults.elevation(),
            onClick = {
                coroutineScope.launch {
                    // TODO: What should I execute at launch?
                }
            },
            icon = {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Favorite"
                )
            },
            text = { Text(stringResource(id = R.string.refresh)) }
        )
    }

    @Composable
    fun ScaffoldContentView(paddingValues: PaddingValues) {
        when (viewModel.viewState.value) {
            MainViewModel.ViewState.ShowTranslationList -> {
                OnShowTranslationList(paddingValues)
            }
            MainViewModel.ViewState.RequestNewGroup -> {
                OnShowNewGroup(paddingValues)
            }
            MainViewModel.ViewState.RequestNewTranslation -> {
                OnShowNewTranslation(paddingValues)
            }
        }
    }

    @Composable
    private fun OnShowNewGroup(paddingValues: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            CreateNewGroupEditText()
            CreateAddNewGroupButton()
            OnFocusOnGroupNameTitle()
        }
    }

    @Composable
    private fun OnFocusOnGroupNameTitle() {
        focusManager.clearFocus(force = true)
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                viewModel.groupNameTitleRequester.requestFocus()
            }
        }
    }

    @Composable
    private fun CreateNewGroupEditText() {
        CreateTextField(
            onValueChange = {
                emit(MainViewModel.Intent.NewGroupPending(it))
            },
            onNextCallback = ::onAddNewGroupButtonClicked,
            mutableState = viewModel.currentGroupTitle,
            placeholder = stringResource(R.string.enter_group_title),
            label = stringResource(R.string.group_title),
            focusRequester = viewModel.groupNameTitleRequester,
        )
    }

    @Composable
    private fun CreateAddNewGroupButton() {
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            enabled = true,
            onClick = ::onAddNewGroupButtonClicked,
        ) {
            Text(stringResource(R.string.add))
        }
    }

    private fun onAddNewGroupButtonClicked() {
        emit(MainViewModel.Intent.SaveNewGroup)
    }

    @Composable
    private fun OnShowNewTranslation(paddingValues: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            CreateNewTranslationFromEditText()
            CreateNewTranslationToEditText()
            CreateAddNewTranslationButton()
            OnFocusOnNewTranslationFrom()
        }
    }

    @Composable
    private fun CreateNewTranslationFromEditText() {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CreateTextField(
                onValueChange = {
                    emit(
                        MainViewModel.Intent.NewTranslationFromPending(
                            it
                        )
                    )
                },
                onNextCallback = {
                    emit(MainViewModel.Intent.ProvidedTranslationFrom)
                },
                mutableState = viewModel.currentTranslationFrom,
                placeholder = stringResource(R.string.enter_original_text),
                label = stringResource(R.string.original_text),
                focusRequester = viewModel.translationFromRequester,
            )
        }
    }

    @Composable
    private fun CreateNewTranslationToEditText() {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CreateTextField(
                onValueChange = {
                    emit(
                        MainViewModel.Intent.NewTranslationToPending(
                            it
                        )
                    )
                },
                onNextCallback = {
                    emit(MainViewModel.Intent.SaveNewTranslation)
                },
                mutableState = viewModel.currentTranslationTo,
                placeholder = stringResource(R.string.enter_Translation_text),
                label = stringResource(R.string.translation_text),
                focusRequester = viewModel.translationToRequester,
            )
        }
    }

    @Composable
    private fun CreateAddNewTranslationButton() {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                enabled = true,
                onClick = {
                    onTranslationToClicked()
                }
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }

    private fun onTranslationToClicked() {
        emit(MainViewModel.Intent.SaveNewTranslation)
    }

    private fun emit(intent: MainViewModel.Intent) {
        coroutineScope.launch { viewModel.emit(intent) }
    }

    @Composable
    private fun OnFocusOnNewTranslationFrom() {
        focusManager.clearFocus(force = true)
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                viewModel.translationFromRequester.requestFocus()
            }
        }
    }

    @Composable
    private fun OnFocusOnNewTranslationTo() {
        focusManager.clearFocus(force = true)
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                viewModel.translationToRequester.requestFocus()
            }
        }
    }

    @Composable
    private fun CreateTextField(
        onValueChange: (String) -> Unit,
        onNextCallback: () -> Unit,
        mutableState: MutableState<String>,
        placeholder: String,
        label: String,
        focusRequester: FocusRequester,
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            enabled = true,
            value = mutableState.value,
            onValueChange = {
                mutableState.value = it
                onValueChange.invoke(it)
            },
            placeholder = {
                Text(text = placeholder)
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    onNextCallback()
                }
            ),
            singleLine = true,
            isError = false,
            visualTransformation = VisualTransformation.None,
            colors = TextFieldDefaults.outlinedTextFieldColors()
        )
    }

    @Composable
    private fun OnViewStateIdle() {
        // TODO: Whenever... for now, do nothing
    }

    @Composable
    private fun OnViewStateInProgress() {
        // TODO Whenever... Display a progress bar on the absolute center of the screen
    }

    @ExperimentalMaterialApi
    @Composable
    private fun OnShowTranslationList(paddingValues: PaddingValues) =
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            items(count = viewModel.currentTranslations.value.size) { index ->
                if(index < viewModel.currentTranslations.value.size) {
                    SwipeToDismissColumn(index)
                }
            }
        }

    @ExperimentalMaterialApi
    @Composable
    fun SwipeToDismissColumn(index: Int) {
        val isDeleted = remember { mutableStateOf(false) }
        val dismissState = rememberDismissState(
            initialValue = DismissValue.Default,
            confirmStateChange = {
                when(it) {
                    DismissValue.DismissedToStart -> {
                        isDeleted.value = true
                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
                    }
                    DismissValue.DismissedToEnd -> {
                        isDeleted.value = true
//                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
                    }
                    DismissValue.Default -> {
                    }
                }
                true
            }
        )

        if(!isDeleted.value && index < viewModel.currentTranslations.value.size) {
            SwipeToDismiss(
                state = dismissState,
                /***  create dismiss alert Background */
                background = {
                    val color = when (dismissState.dismissDirection) {
                        DismissDirection.StartToEnd -> Color.Green
                        DismissDirection.EndToStart -> Color.Red
                        null -> Color.Transparent
                    }
                    val direction = dismissState.dismissDirection

                    if (direction == DismissDirection.StartToEnd) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = "Move to Archive", fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.heightIn(5.dp))
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = "Move to Bin",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )

                            }
                        }
                    }
                },
                /**** Dismiss Content */
                dismissContent = {
                    TranslationCard(index, viewModel.currentTranslations.value[index])
                },
                /*** Set Direction to dismiss */
                directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
            )
        }
    }

    @Composable
    private fun TranslationCard(index: Int, translation: TranslationModel) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        when (val nextTranslation = viewModel.currentTranslations.value[index]) {
                            is TextTranslationGroup -> {
                                Text(
                                    text = nextTranslation.title,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            is TranslationModel.TextTranslationModel -> {
                                Text(
                                    text = nextTranslation.from,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                                Text(
                                    text = nextTranslation.to,
                                    fontStyle = FontStyle.Italic,
                                    color = if(isSystemInDarkTheme()) Color.Cyan else Color.Blue
                                )
                            }
                        }
                    }
                }
            }
        }
    }

//    @ExperimentalMaterialApi
//    @Composable
//    private fun SwipeToDismissColumn(index: Int) {
//        val isDeleted = remember { mutableStateOf(false) }
//        val dismissState = rememberDismissState(
//            initialValue = DismissValue.Default,
//            confirmStateChange = {
//                when(it) {
//                    DismissValue.DismissedToStart -> {
//                        isDeleted.value = true
////                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
//                    }
//                    DismissValue.DismissedToEnd -> {
//                        isDeleted.value = true
////                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
//                    }
//                    DismissValue.Default -> {
//                    }
//                }
//                true
//            }
//        )
//
//        if(!isDeleted.value) {
//            SwipeToDismiss(
//                state = dismissState,
//                /***  create dismiss alert Background */
//                background = {
//                    val color = when (dismissState.dismissDirection) {
//                        DismissDirection.StartToEnd -> Color.Green
//                        DismissDirection.EndToStart -> Color.Red
//                        null -> Color.Transparent
//                    }
//                    val direction = dismissState.dismissDirection
//
//                    if (direction == DismissDirection.StartToEnd) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(color)
//                                .padding(8.dp)
//                        ) {
//                            Column(modifier = Modifier.align(Alignment.CenterStart)) {
//                                Icon(
//                                    imageVector = Icons.Default.ArrowForward,
//                                    contentDescription = null,
//                                    tint = Color.White,
//                                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                                )
//                                Text(
//                                    text = "Move to Archive", fontWeight = FontWeight.Bold,
//                                    textAlign = TextAlign.Center,
//                                    color = Color.White
//                                )
//                            }
//                        }
//                    } else {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(color)
//                                .padding(8.dp)
//                        ) {
//                            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
//                                Icon(
//                                    imageVector = Icons.Default.ArrowBack,
//                                    contentDescription = null,
//                                    tint = Color.White,
//                                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                                )
//                                Spacer(modifier = Modifier.heightIn(5.dp))
//                                Text(
//                                    text = "Move to Bin",
//                                    textAlign = TextAlign.Center,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.LightGray
//                                )
//
//                            }
//                        }
//                    }
//                },
//                /**** Dismiss Content */
//                dismissContent = {
//                    TranslationCard(viewModel.translations.value[index])
//                },
//                /*** Set Direction to dismiss */
//                directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
//            )
//        }
////        val isLastItem = index == viewModel.translations.value.size - 1
////        val isDeleted = remember { mutableStateOf(false) }
////        val dismissState = rememberDismissState(
////            initialValue = DismissValue.Default,
////            confirmStateChange = {
////                when (it) {
////                    DismissValue.DismissedToStart -> {
////                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
////                        isDeleted.value = true
////                    }
////                    DismissValue.DismissedToEnd -> {
////                        emit(MainViewModel.Intent.DeleteGroupAtPosition(index))
////                        isDeleted.value = true
////                    }
////                    DismissValue.Default -> {
////                    }
////                }
////                true
////            }
////        )
////
////        if (!isDeleted.value) {
////            val animateState: State<Dp> =
////                animateDpAsState(targetValue = if (dismissState.dismissDirection != null) 4.dp else 0.dp)
////
////            SwipeToDismiss(
////                state = dismissState,
////                /*** Set Direction to dismiss */
////                directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
////                /***  create dismiss alert Background */
////                background = {
////                    val color = when (dismissState.targetValue) {
////                        DismissValue.Default -> Color.Transparent
////                        DismissValue.DismissedToEnd -> Color.Green
////                        DismissValue.DismissedToStart -> Color.Red
////                    }
////
////                    when (dismissState.dismissDirection) {
////                        DismissDirection.StartToEnd -> {
////                            Box(
////                                modifier = Modifier
////                                    .fillMaxSize()
////                                    .background(color)
////                                    .padding(8.dp)
////                            ) {
////                                Column(modifier = Modifier.align(Alignment.CenterStart)) {
////                                    Icon(
////                                        imageVector = Icons.Default.ArrowForward,
////                                        contentDescription = null,
////                                        tint = Color.White,
////                                        modifier = Modifier.align(Alignment.CenterHorizontally)
////                                    )
////                                    Text(
////                                        text = "Move to Archive", fontWeight = FontWeight.Bold,
////                                        textAlign = TextAlign.Center,
////                                        color = Color.White
////                                    )
////                                }
////                            }
////                        }
////                        DismissDirection.EndToStart -> {
////                            Box(
////                                modifier = Modifier
////                                    .fillMaxSize()
////                                    .background(color)
////                                    .padding(8.dp)
////                            ) {
////                                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
////                                    Icon(
////                                        imageVector = Icons.Default.ArrowBack,
////                                        contentDescription = null,
////                                        tint = Color.White,
////                                        modifier = Modifier.align(Alignment.CenterHorizontally)
////                                    )
////                                    Spacer(modifier = Modifier.heightIn(5.dp))
////                                    Text(
////                                        text = "Move to Bin",
////                                        textAlign = TextAlign.Center,
////                                        fontWeight = FontWeight.Bold,
////                                        color = Color.LightGray
////                                    )
////
////                                }
////                            }
////                        }
////                        else -> {}
////                    }
////                },
////                /**** Dismiss Content */
////                dismissContent = {
////                    Card(
////                        modifier = Modifier
////                            .fillMaxSize()
////                            .padding(
////                                8.dp,
////                                0.dp,
////                                0.dp,
////                                8.dp,
////    //                                    if (isLastItem) 64.dp else 8.dp
////                            ),
////                        elevation = animateState.value
////                    ) {
////                        when (val nextTranslation = viewModel.translations.value[index]) {
////                            is TextTranslationGroup -> {
////                                Text(
////                                    text = nextTranslation.title,
////                                    fontWeight = FontWeight.Bold,
////                                    fontStyle = FontStyle.Italic
////                                )
////                            }
////                            is TranslationModel.TextTranslationModel -> {
////                                Text(
////                                    text = nextTranslation.to,
////                                    fontWeight = FontWeight.Bold,
////                                    fontStyle = FontStyle.Italic
////                                )
////                            }
////                        }
////                    }
////                },
////            )
////
////            Divider(modifier = Modifier.padding(0.dp, 8.dp))
////        }
//    }

//    @Composable
//    private fun TranslationCard(nextTranslation: TranslationModel) {
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Card(
//                shape = RoundedCornerShape(14.dp),
//                backgroundColor = Color.White,
//                modifier = Modifier
//                    .padding(10.dp)
//                    .fillMaxWidth(),
//
//                ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//
//                ) {
//                    Image(
//                        painter = painterResource(android.R.drawable.ic_dialog_alert),
//                        contentDescription = null,
//                        modifier = Modifier.size(65.dp),
//                    )
//                    Row(
//                        modifier = Modifier.padding(start = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            when (nextTranslation) {
//                                is TextTranslationGroup -> {
//                                    Text(
//                                        text = nextTranslation.title,
//                                        fontWeight = FontWeight.Bold,
//                                        fontStyle = FontStyle.Italic,
//                                        fontSize = 16.sp,
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//                                is TranslationModel.TextTranslationModel -> {
//                                    Text(
//                                        color = Color.Gray,
//                                        text = nextTranslation.to,
//                                        fontWeight = FontWeight.Bold,
//                                        fontStyle = FontStyle.Italic,
//                                        fontSize = 16.sp,
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    @Composable
    private fun OnViewStateError(text: String) {
        showToastExample(text)
    }

    private fun showToastExample(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

// endregion

    @Preview(
        name = "Default Preview in Light Mode",
        uiMode = Configuration.UI_MODE_NIGHT_NO,
        showSystemUi = true,
        showBackground = true
    )
    @Composable
    fun DefaultPreview() {
        val data: List<TranslationModel> = listOf(
            TextTranslationGroup(
                title = "English to Spanish",
                translations = listOf(
                    TranslationModel.TextTranslationModel("Hello", "Ola")
                )
            ),
            TextTranslationGroup(
                title = "English to French",
                translations = listOf(
                    TranslationModel.TextTranslationModel("Hello", "Ola")
                )
            ),
            TextTranslationGroup(
                title = "English to Swahilli",
                translations = listOf(
                    TranslationModel.TextTranslationModel("Hello", "Ola")
                )
            )
        )
        MyTranslationNotesTheme {
            ContentView(
                mockType = MockType.FakeSuccess,
                initialList = data,
                viewState = MainViewModel.ViewState.ShowTranslationList,
            )
        }
    }

    @Preview(
        name = "ShowNewGroup Preview",
        uiMode = Configuration.UI_MODE_NIGHT_NO,
        showSystemUi = true,
        showBackground = true
    )
    @Composable
    fun ShowNewGroupPreview() {
        MyTranslationNotesTheme {
            ContentView(
                mockType = MockType.FakeSuccess,
                viewState = MainViewModel.ViewState.RequestNewGroup,
            )
        }
    }

    @Preview(
        name = "ShowNewTranslation Preview",
        uiMode = Configuration.UI_MODE_NIGHT_NO,
        showSystemUi = true,
        showBackground = true
    )
    @Composable
    fun ShowNewTranslationPreview() {
        MyTranslationNotesTheme {
            ContentView(
                mockType = MockType.FakeSuccess,
                viewState = MainViewModel.ViewState.RequestNewTranslation,
            )
        }
    }
}