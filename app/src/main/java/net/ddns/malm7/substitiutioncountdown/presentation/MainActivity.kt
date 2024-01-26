package net.ddns.malm7.substitiutioncountdown.presentation

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalWearFoundationApi::class)
@ExperimentalCoroutinesApi
class MainActivity : ComponentActivity() {
    private val viewModel: CountDownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val timerText by viewModel.timerText.collectAsStateWithLifecycle()
            val lastTimerText by viewModel.lastTimerText.collectAsStateWithLifecycle()
            val runningOver by viewModel.runningOver.collectAsStateWithLifecycle()

            // Vibrate when running over.
            // TODO: I'm not sure this is the place to do this...
            // TODO: Should we pause vibration when not running? How? We can only cancel.
            // TODO: How to run vibration when the app is in the background?
            val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (runningOver && vibrator.hasVibrator()) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(700L, 300L, 50L, 950L, 50L, 950L, 50L, 950L, 50L, 950L, 140L, 860L),
                    intArrayOf(  255,    0,  90,    0,  90,    0,  90,    0,  90,    0,   50,    0),
                    2)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.cancel()
            }

            Scaffold(
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(
                            fontSize = 10.sp
                        )
                    )
                }
            ) {
                val focusRequester = rememberActiveFocusRequester()

                CountDown(
                    modifier = Modifier
                        .fillMaxSize()
                        .onRotaryScrollEvent {
                            viewModel.handleScroll(it.verticalScrollPixels)
                            true
                        }
                        .focusRequester(focusRequester)
                        .focusable(),
                    state = timerState,
                    timerText = timerText,
                    lastTimerText = lastTimerText,
                    runningOver = runningOver,
                    onToggleRunning = viewModel::toggleIsRunning,
                    onRestart = viewModel::restartTimer,
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            event?.startTracking()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            this.viewModel.restartTimer()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            if ((event.flags and KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0) {
                if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                    this.viewModel.toggleIsRunning()
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Preview
@Composable
private fun CountDown(
    modifier: Modifier = Modifier,
    state: TimerState = TimerState.PAUSED,
    timerText: String = "1:15",
    lastTimerText: String = "33",
    runningOver: Boolean = true,
    onToggleRunning: () -> Unit = {},
    onRestart: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timerText,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (runningOver) Color.Red else Color.Unspecified
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onToggleRunning) {
                Icon(
                    imageVector = if (state == TimerState.RUNNING) {
                        Icons.Default.Pause
                    } else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRestart,
                enabled = state != TimerState.RESET
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lastTimerText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}
