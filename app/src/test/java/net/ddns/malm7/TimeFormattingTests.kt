import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.ddns.malm7.substitiutioncountdown.presentation.CountDownViewModel
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeFormattingTests {
    @Test fun test() {
        val hours = 60 * 60 * 1000L
        val minutes = 60 * 1000L
        val seconds = 1000L
        Assert.assertEquals("3:54:05", CountDownViewModel.millisToStr(3 * hours + 54 * minutes + 5 * seconds + 32L))
        Assert.assertEquals("1", CountDownViewModel.millisToStr(1 * seconds + 1L))
        Assert.assertEquals("0", CountDownViewModel.millisToStr(5L))
        Assert.assertEquals("0", CountDownViewModel.millisToStr(0L))
        Assert.assertEquals("-1", CountDownViewModel.millisToStr(-5L))
        Assert.assertEquals("-1", CountDownViewModel.millisToStr(-1 * seconds))
        Assert.assertEquals("-2", CountDownViewModel.millisToStr(-(1 * seconds + 1L)))
        Assert.assertEquals("-1:00", CountDownViewModel.millisToStr(-(60 * seconds)))
        Assert.assertEquals("-32:03:24", CountDownViewModel.millisToStr(-(32 * hours + 3 * minutes + 23 * seconds + 542L)))
    }
}