package com.Rivet.netwarriorlauncher

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.Rivet.netwarriorlauncher.ui.theme.NetWarriorLauncherTheme
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.RectangleShape
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.MutableState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope // <--- ADDED IMPORT
import androidx.compose.runtime.LaunchedEffect // <--- ADDED IMPORT
import kotlinx.coroutines.delay // <--- ADDED IMPORT

class MainActivity : ComponentActivity() {

    // Health data listener instance
    private lateinit var healthDataListener: HealthDataListener

    // State variables to hold health data received from the listener
    // These will trigger UI recompositions when their values change.
    private val batteryLevel = mutableStateOf(22) // Default to 22%
    private val chargingStatus = mutableStateOf(1) // Default to 1 (Pre-charge)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE // Sets the orientation to landscape

        // Initialize your HealthDataListener
        healthDataListener = HealthDataListener()

        // Start listening for health data from SH device
        healthDataListener.startListening(object : HealthDataListener.HealthDataCallback {
            override fun onHealthDataReceived(healthData: HealthData) {
                // Update the UI state on the main thread
                // These assignments will automatically trigger a recomposition of the UI
                // where these mutableStateOf variables are used (e.g., in MainScreen).
                batteryLevel.value = healthData.batteryLevel
                chargingStatus.value = healthData.chargingStatus

                // Log the received data (same format as before)
                Log.i("ButtonEvent", "Received health data: battery=${healthData.batteryLevel}%, status=${healthData.chargingStatus}")
            }

            override fun onError(error: String) {
                Log.e("ButtonEvent", "Health data error: $error")
                // You might want to show a Toast or another UI indicator for errors
            }
        })


        setContent {
            NetWarriorLauncherTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black    // Sets Background color to black
                ) { innerPadding ->
                    // Pass the current batteryLevel and chargingStatus to MainScreen
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        batteryLevel = batteryLevel.value, // Pass the current value
                        chargingStatus = chargingStatus.value // Pass the current value
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the UDP listener when the activity is destroyed
        healthDataListener.stopListening()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, batteryLevel: Int = 22, chargingStatus: Int = 1) {
    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(screenWidth * 0.02f) // 2% of screen width padding
            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
            .padding(screenWidth * 0.02f) // 2% internal padding
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            // Left Section (Brightness and Dimmer Switches)
            Column(
                modifier = Modifier
                    .weight(2.5f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "HUD Settings",
                    color = Color.White,
                    fontSize = (screenWidth * 0.022f).value.sp, // Responsive font size
                    modifier = Modifier.padding(bottom = screenHeight * 0.04f) // 4% of height
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Brightness switch
                    ToggleSwitch(
                        modifier = Modifier.padding(screenWidth * 0.01f), // 1% padding
                        initialState = true,
                        buttonCode = "006c",
                        label = "Brightness",
                        onText = "HIGH",
                        offText = "LOW"
                    )

                    // Dimmer switch
                    ToggleSwitch(
                        modifier = Modifier.padding(screenWidth * 0.01f), // 1% padding
                        initialState = true,
                        buttonCode = "0073",
                        label = "Dimmer",
                        onText = "ON",
                        offText = "OFF"
                    )
                }
            }

            // Middle Section (Buttons)
            Column(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Green),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Apps",
                    color = Color.White,
                    fontSize = (screenWidth * 0.022f).value.sp, // Responsive font size
                    modifier = Modifier.padding(bottom = screenHeight * 0.04f) // 4% of height
                )

                AppButtonGrid() // AppButtonGrid will now manage the selection state
            }

            // Right section (battery)
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Blue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Battery",
                    color = Color.White,
                    fontSize = (screenWidth * 0.022f).value.sp, // Responsive font size
                    modifier = Modifier.padding(bottom = screenHeight * 0.04f) // 4% of height
                )

                BatteryIndicator(
                    batteryLevel = batteryLevel, // This will now reflect the HealthDataListener updates
                    chargingStatus = chargingStatus, // This will now reflect the HealthDataListener updates
                    modifier = Modifier.padding(screenWidth * 0.01f) // 1% padding
                )
            }
        }
    }
}

// Toggle Switch
@Composable
fun ToggleSwitch(
    modifier: Modifier = Modifier,
    initialState: Boolean = true,
    buttonCode: String,
    label: String,
    onText: String = "ON",
    offText: String = "OFF"
){
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

    val isOn = remember { mutableStateOf(initialState)}

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The switch itself
        Box(
            modifier = modifier
                .width(screenWidth * 0.09f) // 9% of screen width
                .height(screenHeight * 0.45f) // 45% of screen height
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                .clickable {
                    isOn.value = !isOn.value

                    // Send a broadcast that mimics the hardware button event
                    val intent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")

                    // Add intent extras to mimic the hardware event format
                    intent.putExtra("event_device", "/dev/input/event0")
                    intent.putExtra("event_type", "0001")  // EV_KEY
                    intent.putExtra("event_code", buttonCode)
                    intent.putExtra("event_value", "00000001") // Press state

                    // Log the event so it can be seen in logcat
                    Log.i("ButtonEvent", "/dev/input/event0: 0001 $buttonCode 00000001")

                    // Send the broadcast
                    context.sendBroadcast(intent)

                    // Send a "button release" event after a short delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val releaseIntent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")
                        releaseIntent.putExtra("event_device", "/dev/input/event0")
                        releaseIntent.putExtra("event_type", "0001")
                        releaseIntent.putExtra("event_code", buttonCode)
                        releaseIntent.putExtra("event_value", "00000000") // Release state
                        Log.i("ButtonEvent", "/dev/input/event0: 0001 $buttonCode 00000000")
                        context.sendBroadcast(releaseIntent)
                    }, 150) // 100ms delay
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ON section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (isOn.value) Color(0, 150, 0, 128) else Color.Transparent
                        )
                ) {
                    Text(
                        text = onText,
                        color = if (isOn.value) Color.White else Color(200, 200, 200),
                        fontSize = (screenWidth * 0.018f).value.sp, // Responsive font
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White)
                )

                // OFF section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (!isOn.value) Color(150, 0, 0, 128) else Color.Transparent
                        )
                ) {
                    Text(
                        text = offText,
                        color = if (!isOn.value) Color.White else Color(200, 200, 200),
                        fontSize = (screenWidth * 0.018f).value.sp, // Responsive font
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )
                }
            }
        }

        // Add the label text below the switch
        Text(
            text = label,
            color = Color.White,
            fontSize = (screenWidth * 0.016f).value.sp, // Slightly smaller font
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = screenHeight * 0.01f) // 1% of height padding
        )
    }
}

// Button Grid - NOW MANAGES SELECTED BUTTON STATE
@Composable
fun AppButtonGrid() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // State to hold the label of the currently selected button (null if none)
    val selectedButtonLabel = remember { mutableStateOf<String?>(null) } // Changed to nullable String

    Column(
        modifier = Modifier
            .padding(
                horizontal = screenWidth * 0.015f,
                vertical = screenHeight * 0.01f
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f) // 2% of screen height spacing
    ) {
        // Row 1
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.015f) // 1.5% of screen width spacing
        ) {
            AppButton(
                label = "Selene",
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
            AppButton(
                label = "Calibrate",
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
            AppButton(
                label = "Low Lights",
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
        }

        // Row 2
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.015f) // 1.5% of screen width spacing
        ) {
            AppButton(
                label = "Thermal",
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
            AppButton(
                label = "Fusion",
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
            AppButton(
                label = "Clean Up", // The target button for special behavior
                selectedButtonLabel = selectedButtonLabel.value,
                onSelect = { label -> selectedButtonLabel.value = label }
            )
        }
    }
}

// App Button - NOW ACCEPTS SELECTED STATE AND CALLBACK
@Composable
fun AppButton(
    label: String,
    selectedButtonLabel: String?, // The label of the currently selected button from parent
    onSelect: (String?) -> Unit // Callback to inform parent of selection (now accepts nullable String)
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

    val unlitColor = Color(0, 0, 150) // Original dark blue
    val litColor = Color(0xFF4FC3F7) // Lighter blue (light blue)

    // Determine the button's current color based on whether its label matches the selected one
    val isThisButtonSelected = (label == selectedButtonLabel)
    val targetColor = if (isThisButtonSelected) litColor else unlitColor

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 150),
        label = "button_color_animation" // Added label for animation
    )

    // Use a CoroutineScope for launching suspend functions (like delay)
    val scope = rememberCoroutineScope()

    // LaunchedEffect to handle the "Clean Up" button's auto-unselection timer
    // It will recompose and potentially restart/cancel if selectedButtonLabel changes or the button's label changes
    LaunchedEffect(selectedButtonLabel, label) {
        // Check if this is the "Clean Up" button and it's currently selected
        if (label == "Clean Up" && isThisButtonSelected) {
            Log.d("CleanUpTimer", "Clean Up button selected. Starting 5s timer.")
            delay(500L) // Wait for 5 seconds
            Log.d("CleanUpTimer", "Clean Up timer expired. Attempting to unselect.")
            // Only unselect if "Clean Up" is still the active button after the delay.
            // This prevents unselecting another button if the user clicked it during the 5s.
            if (selectedButtonLabel == label) {
                onSelect(null) // Unselect "Clean Up"
            }
        }
    }

    Button(
        onClick = {
            if (label == "Clean Up") {
                // If Clean Up is clicked, always set it as selected.
                // The LaunchedEffect above will handle its auto-unselection.
                onSelect(label)
            } else {
                // For other buttons, implement standard radio-button behavior:
                // If this button is already selected, unselect it. Otherwise, select it.
                onSelect(if (isThisButtonSelected) null else label)
            }

            // --- Original broadcast logic for hardware event mimic ---
            val intent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")
            val buttonCode = when(label) {
                "Selene" -> "009e"
                "Calibrate" -> "0072"
                "Low Lights" -> "009f"
                "Thermal" -> "0067"
                "Fusion" -> "007c"
                "Clean Up" -> "0074"
                "App G" -> "00a7"
                "App H" -> "00a8"
                "App I" -> "00a9"
                else -> "00a0"
            }
            intent.putExtra("event_device", "/dev/input/event0")
            intent.putExtra("event_type", "0001")
            intent.putExtra("event_code", buttonCode as String)
            intent.putExtra("event_value", "00000001" as String)
            Log.i("ButtonEvent", "/dev/input/event0: 0001 $buttonCode 00000001")
            context.sendBroadcast(intent)

            // Send a "button release" event after a short delay for the hardware event
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val releaseIntent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")
                val releaseButtonCode = when(label) {
                    "Selene" -> "009e"
                    "Calibrate" -> "0072"
                    "Low Lights" -> "009f"
                    "Thermal" -> "0067"
                    "Fusion" -> "007c"
                    "Clean Up" -> "0074"
                    "App G" -> "00a7"
                    "App H" -> "00a8"
                    "App I" -> "00a9"
                    else -> "00a0"
                }
                releaseIntent.putExtra("event_device", "/dev/input/event0")
                releaseIntent.putExtra("event_type", "0001")
                releaseIntent.putExtra("event_code", releaseButtonCode as String)
                releaseIntent.putExtra("event_value", "00000000" as String)
                Log.i("ButtonEvent", "/dev/input/event0: 0001 $releaseButtonCode 00000000")
                context.sendBroadcast(releaseIntent)
            }, 100) // 100ms delay for hardware event
        },
        modifier = Modifier.size(screenWidth * 0.09f),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedColor
        ),
        contentPadding = PaddingValues(screenWidth * 0.005f)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = (screenWidth * 0.018f).value.sp,
            textAlign = TextAlign.Center
        )
    }
}

// UX System Health Code
@Composable
fun LightUpButton(
    offText: String,
    isOn: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val responsiveFontSize = (screenWidthDp * 0.018f).sp
    val buttonColor = if (isOn.value) Color(0xFF4FC3F7) else Color(0xFF2196F3)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = buttonColor, shape = RoundedCornerShape(8.dp))
            .clickable { isOn.value = !isOn.value }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = offText,
            color = if (!isOn.value) Color.White else Color(200, 200, 200),
            fontSize = responsiveFontSize,
            textAlign = TextAlign.Center
        )
    }
}

// Battery Indicator
@Composable
fun BatteryIndicator(
    batteryLevel: Int,
    chargingStatus: Int = 1,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val batteryColor = when {
        batteryLevel > 60 -> Color(0, 200, 0) // Green for high battery
        batteryLevel > 20 -> Color(255, 255, 0) // Reverted to Yellow for medium battery
        else -> Color(200, 0, 0) // Red for low battery
    }

    // Convert status number to text
    val statusText = when (chargingStatus) {
        0 -> "Initialization"
        1 -> "Pre-charge"
        2 -> "Charging"
        3 -> "Discharging"
        4 -> "No Charging"
        5 -> "Full"
        else -> "Unknown"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Battery box (outline)
        val batteryOutlineWidth = screenWidth * 0.09f
        val batteryOutlineHeight = screenHeight * 0.45f
        val batteryInnerPadding = screenWidth * 0.01f // Padding inside the border

        Box(
            modifier = Modifier
                .width(batteryOutlineWidth)
                .height(batteryOutlineHeight)
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                .padding(batteryInnerPadding) // Applies padding to the content inside this Box
        ) {
            // Battery percentage text - positioned at the top
            Text(
                text = "$batteryLevel%",
                color = Color.White,
                fontSize = (screenWidth * 0.022f).value.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.02f) // Pushes text slightly down from the top edge of the inner padded area
            )

            // Battery level indicator (the colored fill bar)
            // It needs to grow from the bottom of the fillable area.
            val fillableHeight =( batteryOutlineHeight - (batteryInnerPadding *  2))
            val currentFillHeight = (fillableHeight.value * batteryLevel / 100f).dp

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Align this Box's bottom edge with its parent's bottom edge
                    .fillMaxWidth() // Fill the available width within the parent's padding
                    .height(currentFillHeight) // Set the dynamic height based on battery level
                    .background(
                        batteryColor,
                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                    )
            )
        }

        // Status text below the battery box
        Text(
            text = statusText,
            color = Color.White,
            fontSize = (screenWidth * 0.016f).value.sp, // Smaller font
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = screenHeight * 0.01f) // Small gap above text
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES  // Force dark mode
)
@Composable
fun MainScreenPreview() {
    NetWarriorLauncherTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Black
        ) { innerPadding ->
            MainScreen(
                modifier = Modifier.padding(innerPadding),
                batteryLevel = 75, // Example preview value
                chargingStatus = 2 // Example preview value (Charging)
            )
        }
    }
}
