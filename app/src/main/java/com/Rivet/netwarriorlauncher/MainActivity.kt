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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
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
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE //Sets the orientation to landscape
        setContent {
            NetWarriorLauncherTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black    // Sets Background color to black
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            // Left Section (Brightness and Dimmer Switches)
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "HUD Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Brightness switch
                    ToggleSwitch(
                        modifier = Modifier.padding(8.dp),
                        initialState = true,
                        buttonCode = "006c"
                    )

                    // Dimmer switch
                    ToggleSwitch(
                        modifier = Modifier.padding(8.dp),
                        initialState = true,
                        buttonCode = "0073"
                    )
                }
            }

            // Middle Section (Buttons)
            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Green),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Buttons",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AppButtonGrid()
            }

            // Right section (will contain battery)
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Blue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Battery",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                BatteryIndicator(
                    batteryLevel = 100,
                    modifier = Modifier.padding(8.dp)
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
    context: Context = androidx.compose.ui.platform.LocalContext.current
){
    val isOn = remember { mutableStateOf(initialState)}

    Box(
        modifier = modifier
            .width(80.dp)
            .height(180.dp)
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
                    text = "ON",
                    color = if (isOn.value) Color.White else Color(200, 200, 200),
                    fontSize = 16.sp,
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
                    text = "OFF",
                    color = if (!isOn.value) Color.White else Color(200, 200, 200),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }
        }
    }
}


// Button Grid
@Composable
fun AppButtonGrid() {
    Column(
        modifier = Modifier
            .padding(12.dp, top=0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(label = "App A")
            AppButton(label = "App B")
            AppButton(label = "App C")
        }

        // Row 2
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(label = "App D")
            AppButton(label = "App E")
            AppButton(label = "App F")
        }

        // Row 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(label = "App G")
            AppButton(label = "App H")
            AppButton(label = "App I")
        }
    }
}


/*
// Button
@Composable
fun AppButton(label: String) {
    Button(
        onClick = { /* Button click handler will be added later */ },
        modifier = Modifier.size(80.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0, 0, 150)
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
*/

// Button
@Composable
fun AppButton(label: String, context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Button(
        onClick = {
            // Send a broadcast that mimics the hardware button event
            val intent = Intent("com.Rivet.netwarriorlauncher.BUTTON_EVENT")

            // Use the button label to create a unique "code" for each button
            val buttonCode = when(label) {
                "App A" -> "00a1"
                "App B" -> "00a2"
                "App C" -> "00a3"
                "App D" -> "00a4"
                "App E" -> "00a5"
                "App F" -> "00a6"
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
        modifier = Modifier.size(80.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0, 0, 150)
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Battery Indicator
@Composable
fun BatteryIndicator(
    batteryLevel: Int,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        batteryLevel > 60 -> Color(0, 200, 0) // Green for high battery
        batteryLevel > 20 -> Color(200, 200, 0) // Yellow for medium battery
        else -> Color(200, 0, 0) // Red for low battery
    }
    Box(
        modifier = modifier
            .width(80.dp)
            .height(180.dp)
            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        // Battery percentage text
        Text(
            text = "$batteryLevel%",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // Battery level indicator
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 32.dp) // Space for the text above
                .fillMaxWidth()
                .height(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Battery level representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((120.dp * batteryLevel / 100))
                    .background(
                        batteryColor,
                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                    )
            )
        }
    }

}


@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun MainScreenPreview() {
    NetWarriorLauncherTheme {
        MainScreen()
    }
}