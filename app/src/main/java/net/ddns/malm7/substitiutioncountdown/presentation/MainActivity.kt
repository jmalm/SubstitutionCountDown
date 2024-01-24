package net.ddns.malm7.substitiutioncountdown.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
@ExperimentalCoroutinesApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<CountDownViewModel>()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val timerText by viewModel.timerText.collectAsStateWithLifecycle()
            val lastTimerText by viewModel.lastTimerText.collectAsStateWithLifecycle()
            val runningOver by viewModel.runningOver.collectAsStateWithLifecycle()

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
                val coroutineScope = rememberCoroutineScope()

                CountDown(
                    modifier = Modifier
                        .fillMaxSize()
                        .onRotaryScrollEvent {
                            coroutineScope.launch {
                                viewModel.handleScroll(it.verticalScrollPixels)
                            }
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
            fontSize = 32.sp,
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}
