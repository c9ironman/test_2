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


class MainActivity : ComponentActivity() {
    //private lateinit var batteryReceiver: BroadcastReceiver
    // state variable

    // Health data listener
    private lateinit var healthDataListener: HealthDataListener
    private val batteryLevel = mutableStateOf(22) // Default to 22%
    private val chargingStatus = mutableStateOf(1) // Add charging status state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE // Sets the orientation to landscape

//        batteryReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                if (intent.action == "com.Rivet.netwarriorlauncher.BATTERY_UPDATE") {
//                    val level = intent.getIntExtra("battery_level", -1)
//                    if (level >= 0) {
//                        // Log in the same format as your ButtonEvent logs
//                        batteryLevel.value = level
//                        Log.i("ButtonEvent", "Received battery update: level=$level%")
//                    }
//                }
//            }
//        }

//        // Register the receiver
//        val filter = IntentFilter("com.Rivet.netwarriorlauncher.BATTERY_UPDATE")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            // For API 33+ (Android 13+)
//            registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED)
//        } else {
//            // For older API versions
//            registerReceiver(batteryReceiver, filter)
//        }

        healthDataListener = HealthDataListener()

        // Start listening for health data from SH device
        healthDataListener.startListening(object : HealthDataListener.HealthDataCallback {
            override fun onHealthDataReceived(healthData: HealthData) {
                // Update the UI state on main thread
                batteryLevel.value = healthData.batteryLevel
                chargingStatus.value = healthData.chargingStatus

                // Log the received data (same format as before)
                Log.i("ButtonEvent", "Received health data: battery=${healthData.batteryLevel}%, status=${healthData.chargingStatus}")
            }

            override fun onError(error: String) {
                Log.e("ButtonEvent", "Health data error: $error")
            }
        })


        setContent {
            NetWarriorLauncherTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black    // Sets Background color to black
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        batteryLevel = batteryLevel.value,
                        chargingStatus = chargingStatus.value)
                }
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(batteryReceiver)
//    }

    override fun onDestroy() {
        super.onDestroy()
       //Stop the UDP listener
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
                    text = "Buttons",
                    color = Color.White,
                    fontSize = (screenWidth * 0.022f).value.sp, // Responsive font size
                    modifier = Modifier.padding(bottom = screenHeight * 0.04f) // 4% of height
                )

                AppButtonGrid()
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
                    batteryLevel = batteryLevel,
                    chargingStatus = chargingStatus,
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
                    }, 100) // 100ms delay
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

// Button Grid
@Composable
fun AppButtonGrid() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

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
            AppButton(label = "Selene")
            AppButton(label = "Calibrate")
            AppButton(label = "Low Lights")
        }

        // Row 2
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.015f) // 1.5% of screen width spacing
        ) {
            AppButton(label = "Thermal")
            AppButton(label = "Fusion")
            AppButton(label = "Clean Up")
        }

        // Row 3
        /*
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.015f) // 1.5% of screen width spacing
        ) {
            AppButton(label = "App G")
            AppButton(label = "App H")
            AppButton(label = "App I")
        }

         */
    }
}

// Button
@Composable
fun AppButton(label: String) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

    Button(
        onClick = {
            // Send a broadcast that mimics the hardware button event
            val intent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")

            // Use the button label to create a unique "code" for each button
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

            // Add intent extras to mimic the hardware event format
            intent.putExtra("event_device", "/dev/input/event0")
            intent.putExtra("event_type", "0001")  // EV_KEY
            intent.putExtra("event_code", buttonCode)
            intent.putExtra("event_value", "00000001") // Press state

            // Log the event so it can be seen in logcat
            Log.i("ButtonEvent", "/dev/input/event0: 0001 $buttonCode 00000001")

            // Send the broadcast
            context.sendBroadcast(intent)

            // Optional: Also send a "button release" event after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val releaseIntent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")
                releaseIntent.putExtra("event_device", "/dev/input/event0")
                releaseIntent.putExtra("event_type", "0001")
                releaseIntent.putExtra("event_code", buttonCode)
                releaseIntent.putExtra("event_value", "00000000") // Release state
                Log.i("ButtonEvent", "/dev/input/event0: 0001 $buttonCode 00000000")
                context.sendBroadcast(releaseIntent)
            }, 100) // 100ms delay
        },
        modifier = Modifier.size(screenWidth * 0.09f), // 9% of screen width
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0, 0, 150)
        ),
        contentPadding = PaddingValues(screenWidth * 0.005f) // 0.5% of width
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = (screenWidth * 0.018f).value.sp, // Responsive font size
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
        batteryLevel > 20 -> Color(200, 200, 0) // Yellow for medium battery
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
        // Battery box (existing code)
        Box(
            modifier = Modifier
                .width(screenWidth * 0.09f)
                .height(screenHeight * 0.45f)
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                .padding(screenWidth * 0.01f)
        ) {
            // Battery percentage text
            Text(
                text = "$batteryLevel%",
                color = Color.White,
                fontSize = (screenWidth * 0.022f).value.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.02f)
            )

            // Battery level indicator
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = screenHeight * 0.08f)
                    .fillMaxWidth()
                    .height(screenHeight * 0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight * 0.3f * batteryLevel / 100))
                        .background(
                            batteryColor,
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                        )
                )
            }
        }

        // NEW: Status text below the battery box
        Text(
            text = statusText,
            color = Color.White,
            fontSize = (screenWidth * 0.016f).value.sp, // Smaller font
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = screenHeight * 0.01f) // Small gap above text
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun MainScreenPreview() {
    NetWarriorLauncherTheme {
        MainScreen()
    }
}