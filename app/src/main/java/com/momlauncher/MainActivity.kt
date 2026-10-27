package com.momlauncher

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.momlauncher.call.CallActivity
import com.momlauncher.call.CallManager
import com.momlauncher.data.*
import com.momlauncher.ui.calllog.CallLogScreen
import com.momlauncher.ui.contacts.AddContactDialog
import com.momlauncher.ui.contacts.ContactsScreen
import com.momlauncher.ui.dialpad.DialpadScreen
import com.momlauncher.ui.gallery.GalleryScreen
import com.momlauncher.ui.home.HomeScreen
import com.momlauncher.ui.lock.LockScreen
import com.momlauncher.ui.settings.SettingsScreen
import com.momlauncher.ui.allapps.AllAppsScreen
import com.momlauncher.ui.theme.MomLauncherTheme
import com.momlauncher.ui.theme.PureBlack
import com.momlauncher.data.NewsItem
import com.momlauncher.data.NewsRepository
import com.momlauncher.data.AppsRepository
import com.momlauncher.data.AppItem
import com.momlauncher.ui.news.NewsScreen
import com.momlauncher.util.CallHelper
import com.momlauncher.util.PermissionHelper
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenState {
    HOME,
    CONTACTS,
    DIALPAD,
    CALL_LOG,
    GALLERY,
    NEWS,
    SETTINGS,
    ALL_APPS
}

class MainActivity : ComponentActivity() {

    private lateinit var emergencyRepo: EmergencyContactsRepository
    private lateinit var contactsRepo: ContactsRepository
    private lateinit var callLogRepo: CallLogRepository
    private lateinit var photosRepo: PhotosRepository
    private lateinit var prayerRepo: PrayerTimesRepository
    private lateinit var newsRepo: NewsRepository
    private lateinit var appsRepo: AppsRepository

    private val currentScreen = mutableStateOf(ScreenState.HOME)
    private val previousScreenBeforePicking = mutableStateOf(ScreenState.HOME)
    private val pickingSlot = mutableStateOf<Int?>(null)

    private val emergencyContactsList = mutableStateOf<List<EmergencyContact?>>(emptyList())
    private val installedAppsList = mutableStateOf<List<AppItem>>(emptyList())
    private val isAppsLoadingState = mutableStateOf(false)
    private val allContactsList = mutableStateOf<List<ContactItem>>(emptyList())
    private val callLogsList = mutableStateOf<List<CallLogEntry>>(emptyList())
    private val photosList = mutableStateOf<List<PhotoItem>>(emptyList())
    private val newsList = mutableStateOf<List<NewsItem>>(emptyList())
    private val isNewsLoadingState = mutableStateOf(false)
    private val nextPrayerState = mutableStateOf<NextPrayer?>(null)
    private val missedCallsList = mutableStateOf<List<CallLogEntry>>(emptyList())

    private val hasPermissionsState = mutableStateOf(false)
    private val hasPhotoPermissionState = mutableStateOf(false)
    private val isDefaultDialerState = mutableStateOf(false)
    private val isDefaultHomeState = mutableStateOf(false)
    private val isScreenLocked = mutableStateOf(false)
    private val showAddContactDialog = mutableStateOf(false)
    private val addContactInitialNumber = mutableStateOf("")

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenLocked.value = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (km?.isKeyguardLocked == true) {
                        isScreenLocked.value = true
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowFlags()

        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, screenFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, screenFilter)
        }

        checkLockState()

        emergencyRepo = EmergencyContactsRepository(applicationContext)
        contactsRepo = ContactsRepository(applicationContext)
        callLogRepo = CallLogRepository(applicationContext)
        photosRepo = PhotosRepository(applicationContext)
        prayerRepo = PrayerTimesRepository(applicationContext)
        newsRepo = NewsRepository(applicationContext)
        appsRepo = AppsRepository(applicationContext)

        // Güncel haberleri arka planda önceden yükle
        loadNews(forceRefresh = false)

        // Namaz vakitlerini arka planda periyodik olarak güncelle
        lifecycleScope.launch {
            while (isActive) {
                loadPrayerTimes()
                kotlinx.coroutines.delay(60_000)
            }
        }

        // Launcher geri tuşu davranışı: Ana ekrandayken geri tuşu asla launcher'ı kapatmaz
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isScreenLocked.value) {
                    // Kilit ekranındayken geri tuşunu yut
                    return
                }
                if (currentScreen.value != ScreenState.HOME) {
                    currentScreen.value = ScreenState.HOME
                    pickingSlot.value = null
                } else {
                    // Ana ekrandayken geri tuşunu yut, sistem launcher'ına düşmesini engelle
                }
            }
        })

        setContent {
            MomLauncherTheme {
                val callModel by CallManager.callState.collectAsState()
                var searchQuery by remember { mutableStateOf("") }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    refreshPermissionsAndData()
                }

                val photoPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    refreshPermissionsAndData()
                    if (PermissionHelper.hasPhotoPermission(this@MainActivity)) {
                        loadPhotos()
                    }
                }

                val dialerRoleLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    refreshPermissionsAndData()
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                    color = PureBlack
                ) {
                    if (isScreenLocked.value) {
                        LockScreen(
                            nextPrayer = nextPrayerState.value,
                            missedCalls = missedCallsList.value,
                            onUnlock = { unlockScreen() }
                        )
                    } else {
                        when (currentScreen.value) {
                            ScreenState.HOME -> {
                            HomeScreen(
                                callModel = callModel,
                                emergencyContacts = emergencyContactsList.value,
                                nextPrayer = nextPrayerState.value,
                                missedCalls = missedCallsList.value,
                                onCallBackMissedCall = { entry ->
                                    callLogRepo.dismissMissedCall(entry.id)
                                    loadMissedCalls()
                                    com.momlauncher.util.CallHelper.makeCall(this@MainActivity, entry.number)
                                },
                                onDismissMissedCall = { callId ->
                                    callLogRepo.dismissMissedCall(callId)
                                    loadMissedCalls()
                                },
                                onOpenContacts = {
                                    searchQuery = ""
                                    pickingSlot.value = null
                                    previousScreenBeforePicking.value = ScreenState.HOME
                                    currentScreen.value = ScreenState.CONTACTS
                                    loadContacts("")
                                },
                                onOpenDialpad = {
                                    currentScreen.value = ScreenState.DIALPAD
                                },
                                onOpenCallLog = {
                                    currentScreen.value = ScreenState.CALL_LOG
                                    loadCallLogs()
                                },
                                onOpenGallery = {
                                    currentScreen.value = ScreenState.GALLERY
                                    if (PermissionHelper.hasPhotoPermission(this@MainActivity)) {
                                        loadPhotos()
                                    } else {
                                        photoPermissionLauncher.launch(PermissionHelper.getPhotoPermissions())
                                    }
                                },
                                onOpenNews = {
                                    currentScreen.value = ScreenState.NEWS
                                    loadNews(forceRefresh = false)
                                },
                                onOpenSettings = {
                                    currentScreen.value = ScreenState.SETTINGS
                                },
                                onOpenAllApps = {
                                    Toast.makeText(this@MainActivity, "Torun Modu 👶", Toast.LENGTH_SHORT).show()
                                    currentScreen.value = ScreenState.ALL_APPS
                                    loadInstalledApps()
                                },
                                onSelectEmergencySlot = { slot ->
                                    searchQuery = ""
                                    pickingSlot.value = slot
                                    previousScreenBeforePicking.value = ScreenState.HOME
                                    currentScreen.value = ScreenState.CONTACTS
                                    loadContacts("")
                                },
                                onRequestPermissions = {
                                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                                },
                                onRequestDialerRole = {
                                    PermissionHelper.requestDialerRole(this@MainActivity, dialerRoleLauncher)
                                },
                                onSetDefaultHome = {
                                    PermissionHelper.openHomeSettings(this@MainActivity)
                                },
                                hasPermissions = hasPermissionsState.value,
                                isDefaultDialer = isDefaultDialerState.value,
                                isDefaultHome = isDefaultHomeState.value
                            )
                        }

                        ScreenState.CONTACTS -> {
                            val activeSlot = pickingSlot.value
                            val title = if (activeSlot != null) {
                                "Acil Kişi Seç (${activeSlot + 1}. Yuva)"
                            } else null

                            ContactsScreen(
                                contacts = allContactsList.value,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { q ->
                                    searchQuery = q
                                    loadContacts(q)
                                },
                                onAddNewContact = {
                                    addContactInitialNumber.value = ""
                                    showAddContactDialog.value = true
                                },
                                onContactClicked = { contact ->
                                    if (activeSlot != null) {
                                        emergencyRepo.saveContact(
                                            slotIndex = activeSlot,
                                            name = contact.name,
                                            number = contact.number,
                                            photoUri = contact.photoUri
                                        )
                                        pickingSlot.value = null
                                        currentScreen.value = ScreenState.HOME
                                        refreshEmergencyContacts()
                                    } else {
                                        CallHelper.makeCall(this@MainActivity, contact.number)
                                    }
                                },
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                    pickingSlot.value = null
                                },
                                pickingTitle = title
                            )
                        }

                        ScreenState.DIALPAD -> {
                            DialpadScreen(
                                onCall = { number ->
                                    CallHelper.makeCall(this@MainActivity, number)
                                },
                                onAddContact = { number ->
                                    addContactInitialNumber.value = number
                                    showAddContactDialog.value = true
                                },
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }

                        ScreenState.CALL_LOG -> {
                            CallLogScreen(
                                callLogs = callLogsList.value,
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }

                        ScreenState.GALLERY -> {
                            GalleryScreen(
                                photos = photosList.value,
                                hasPermission = hasPhotoPermissionState.value,
                                onRequestPermission = {
                                    photoPermissionLauncher.launch(PermissionHelper.getPhotoPermissions())
                                },
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }

                        ScreenState.NEWS -> {
                            NewsScreen(
                                news = newsList.value,
                                isLoading = isNewsLoadingState.value,
                                onRefresh = {
                                    loadNews(forceRefresh = true)
                                },
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }

                        ScreenState.SETTINGS -> {
                            SettingsScreen(
                                emergencyContacts = emergencyContactsList.value,
                                onSelectEmergencySlot = { slot ->
                                    searchQuery = ""
                                    pickingSlot.value = slot
                                    previousScreenBeforePicking.value = ScreenState.SETTINGS
                                    currentScreen.value = ScreenState.CONTACTS
                                    loadContacts("")
                                },
                                onRemoveEmergencySlot = { slot ->
                                    emergencyRepo.removeContact(slot)
                                    refreshEmergencyContacts()
                                },
                                onRequestDialerRole = {
                                    PermissionHelper.requestDialerRole(this@MainActivity, dialerRoleLauncher)
                                },
                                isDefaultDialer = isDefaultDialerState.value,
                                isDefaultHome = isDefaultHomeState.value,
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }

                        ScreenState.ALL_APPS -> {
                            AllAppsScreen(
                                apps = installedAppsList.value,
                                isLoading = isAppsLoadingState.value,
                                onLaunchApp = { app ->
                                    val ok = appsRepo.launchApp(app.packageName)
                                    if (!ok) {
                                        Toast.makeText(this@MainActivity, "Uygulama açılamadı", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onBack = {
                                    currentScreen.value = ScreenState.HOME
                                }
                            )
                        }
                    }

                    if (showAddContactDialog.value) {
                        AddContactDialog(
                            initialNumber = addContactInitialNumber.value,
                            onDismiss = { showAddContactDialog.value = false },
                            onSave = { name, number ->
                                lifecycleScope.launch {
                                    val success = contactsRepo.addContact(name, number)
                                    if (success) {
                                        Toast.makeText(this@MainActivity, "$name rehbere eklendi", Toast.LENGTH_SHORT).show()
                                        loadContacts("")
                                    } else {
                                        Toast.makeText(this@MainActivity, "Kişi eklenemedi, lütfen izinleri kontrol edin", Toast.LENGTH_SHORT).show()
                                    }
                                    showAddContactDialog.value = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    checkAndRequestInitialPermissions()
}

override fun onDestroy() {
    super.onDestroy()
    try {
        unregisterReceiver(screenReceiver)
    } catch (_: Exception) {
    }
}

override fun onStop() {
    super.onStop()
    val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
    if (powerManager?.isInteractive == false) {
        isScreenLocked.value = true
    }
}

override fun onResume() {
    super.onResume()
    hideSystemBars()
    checkLockState()
    checkActiveCallAndRedirect()
    refreshPermissionsAndData()
}

override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
        hideSystemBars()
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    hideSystemBars()
    checkLockState()
    checkActiveCallAndRedirect()
    if (intent.hasCategory(Intent.CATEGORY_HOME) || intent.action == Intent.ACTION_MAIN) {
        if (currentScreen.value != ScreenState.HOME) {
            currentScreen.value = ScreenState.HOME
        }
    }
}

private fun checkLockState() {
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
    if (keyguardManager?.isKeyguardLocked == true || powerManager?.isInteractive == false) {
        isScreenLocked.value = true
    }
}

private fun unlockScreen() {
    isScreenLocked.value = false
    try {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager?.requestDismissKeyguard(this, null)
        }
    } catch (_: Exception) {
    }
}

private fun setupWindowFlags() {
    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
    hideSystemBars()
}

private fun hideSystemBars() {
    try {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    } catch (_: Throwable) {
    }
}

private fun checkActiveCallAndRedirect() {
    if (CallManager.callState.value.hasActiveCall) {
        val callIntent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(callIntent)
    }
}

    private fun checkAndRequestInitialPermissions() {
        refreshPermissionsAndData()
    }

    private fun refreshPermissionsAndData() {
        hasPermissionsState.value = PermissionHelper.hasAllPermissions(this)
        hasPhotoPermissionState.value = PermissionHelper.hasPhotoPermission(this)
        isDefaultDialerState.value = PermissionHelper.isDefaultDialer(this)
        isDefaultHomeState.value = PermissionHelper.isDefaultHome(this)
        refreshEmergencyContacts()

        if (hasPermissionsState.value) {
            loadMissedCalls()
            when (currentScreen.value) {
                ScreenState.CONTACTS -> loadContacts("")
                ScreenState.CALL_LOG -> loadCallLogs()
                else -> {}
            }
        }
        if (hasPhotoPermissionState.value && currentScreen.value == ScreenState.GALLERY) {
            loadPhotos()
        }
        loadPrayerTimes()
    }

    private fun loadMissedCalls() {
        if (!hasPermissionsState.value) return
        lifecycleScope.launch {
            try {
                missedCallsList.value = callLogRepo.getRecentMissedCalls(limit = 3)
            } catch (_: Exception) {
                missedCallsList.value = emptyList()
            }
        }
    }

    private fun loadPrayerTimes() {
        lifecycleScope.launch {
            try {
                nextPrayerState.value = prayerRepo.getNextPrayer()
            } catch (e: Exception) {
                nextPrayerState.value = null
            }
        }
    }

    private fun refreshEmergencyContacts() {
        emergencyContactsList.value = emergencyRepo.getContacts()
    }

    private fun loadContacts(query: String) {
        lifecycleScope.launch {
            allContactsList.value = contactsRepo.getContacts(query)
        }
    }

    private fun loadCallLogs() {
        lifecycleScope.launch {
            callLogsList.value = callLogRepo.getCallLogs()
        }
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            photosList.value = photosRepo.getPhotos()
        }
    }

    private fun loadNews(forceRefresh: Boolean = false) {
        lifecycleScope.launch {
            isNewsLoadingState.value = true
            newsList.value = newsRepo.getNews(forceRefresh)
            isNewsLoadingState.value = false
        }
    }

    private fun loadInstalledApps() {
        lifecycleScope.launch {
            isAppsLoadingState.value = true
            installedAppsList.value = appsRepo.getInstalledApps()
            isAppsLoadingState.value = false
        }
    }
}
