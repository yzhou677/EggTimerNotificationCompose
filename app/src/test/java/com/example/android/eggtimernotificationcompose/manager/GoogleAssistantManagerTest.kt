import android.content.res.Resources
import android.widget.Toast
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.di.ToastProvider
import com.example.android.eggtimernotificationcompose.manager.GoogleAssistantManager
import com.example.android.eggtimernotificationcompose.manager.TimerAction
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GoogleAssistantManagerTest {
    private lateinit var googleAssistantManager: GoogleAssistantManager
    private val resources: Resources = mock()
    private val toastProvider: ToastProvider = mock()
    private val isTesting: Boolean = true
    private val timerAction: TimerAction = mock()

    @Before
    fun setUp() {
        googleAssistantManager = GoogleAssistantManager(resources, toastProvider, isTesting)
    }

    @Test
    fun `startTimerThroughGoogleAssistant should start timer and update live data for valid softness level`() {
        val softnessLevel = "Soft"
        val timeSelection = 1

        whenever(resources.getStringArray(R.array.egg_array)).thenReturn(
            arrayOf(
                "Soft",
                "Medium",
                "Hard"
            )
        )
        googleAssistantManager.startTimerThroughGoogleAssistant(softnessLevel, timerAction)

        verify(timerAction).startTimer(timeSelection)
        verify(timerAction).updateLiveDataForTimerStartAction(timeSelection)
    }

    @Test
    fun `startTimerThroughGoogleAssistant should show error for invalid softness level`() {
        val invalidSoftnessLevel = "Invalid"
        val supportedLevels = arrayOf("Soft", "Medium", "Hard")

        whenever(resources.getStringArray(R.array.egg_array)).thenReturn(supportedLevels)

        googleAssistantManager.startTimerThroughGoogleAssistant(invalidSoftnessLevel, timerAction)

        val expectedMessage =
            "Invalid softness level. Please choose from: ${supportedLevels.joinToString(", ")}"
        verify(toastProvider).showToast(expectedMessage, Toast.LENGTH_LONG)
    }

    @Test
    fun `showInvalidSoftnessLevelError should show correct toast message`() {
        val supportedLevels = arrayOf("Soft", "Medium", "Hard")
        whenever(resources.getStringArray(R.array.egg_array)).thenReturn(supportedLevels)

        googleAssistantManager.showInvalidSoftnessLevelError()

        val expectedMessage =
            "Invalid softness level. Please choose from: ${supportedLevels.joinToString(", ")}"
        verify(toastProvider).showToast(expectedMessage, Toast.LENGTH_LONG)
    }
}