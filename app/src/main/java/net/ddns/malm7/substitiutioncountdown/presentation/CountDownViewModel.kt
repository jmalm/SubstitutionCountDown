package net.ddns.malm7.substitiutioncountdown.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.math.abs
import kotlin.math.floor

@ExperimentalCoroutinesApi
class CountDownViewModel: ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L)
    val runningOver = _elapsedTime
        .map { millis ->
            millis > _startTimeMillis.value
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    private val _lastElapsedTime = MutableStateFlow(0L)
    val lastTimerText = _lastElapsedTime
        .map { millis -> if (millis > 0) millisToStr(millis) else "" }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ""
        )

    private val _timerState = MutableStateFlow(TimerState.RESET)
    val timerState = _timerState.asStateFlow()

    private val _startTimeMillis = MutableStateFlow(3_000L) // 3 seconds, for quick testing

    val timerText = _elapsedTime
        .map { millis ->
                val millisLeft = _startTimeMillis.value - millis
            millisToStr(millisLeft + 999L) // Show the second we're on.
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "3"
        )

    init {
        _timerState
            .flatMapLatest { timerState ->
                getTimerFlow(
                    isRunning = timerState == TimerState.RUNNING
                )
            }
            .onEach { timeDiff ->
                _elapsedTime.update { it + timeDiff }
            }
            .launchIn(viewModelScope)
    }

    fun toggleIsRunning() {
        when(timerState.value) {
            TimerState.RUNNING -> _timerState.update { TimerState.PAUSED }
            TimerState.PAUSED,
            TimerState.RESET -> _timerState.update { TimerState.RUNNING }
        }
    }

    fun restartTimer() {
        _lastElapsedTime.update { _elapsedTime.value }
        _elapsedTime.update { 0L }
        if (timerState.value == TimerState.PAUSED) _timerState.update { TimerState.RESET }
    }

    private fun getTimerFlow(isRunning: Boolean): Flow<Long> {
        return flow {
            var startMillis = System.currentTimeMillis()
            while (isRunning) {
                val currentMillis = System.currentTimeMillis()
                val timeDiff = if (currentMillis > startMillis) {
                    currentMillis - startMillis
                } else 0L
                emit(timeDiff)
                startMillis = System.currentTimeMillis()
                delay(10L)
            }
        }
    }

    companion object {
        fun millisToStr(millis: Long): String {
            val totalSecondsLeftCeil = floor(millis / 1000F)
            val hoursLeft = abs(totalSecondsLeftCeil / (60 * 60)).toInt()
            val minutesLeft = ((abs(totalSecondsLeftCeil) - hoursLeft * 60 * 60) / 60).toInt()
            val secondsLeft = (abs(totalSecondsLeftCeil) - hoursLeft * 60 * 60 - minutesLeft * 60).toInt()
            val minusSign = if (totalSecondsLeftCeil < 0) "-" else ""
            var time = if (hoursLeft != 0) "$hoursLeft:" else ""
            time += if (hoursLeft != 0 || minutesLeft != 0) minutesLeft.toString()
                .padStart(if (time.isNotEmpty()) 2 else 0, '0') + ":" else ""
            time += secondsLeft.toString().padStart(if (time.isNotEmpty()) 2 else 0, '0')

            return minusSign.plus(time)
        }
    }
}