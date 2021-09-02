import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(decimals: Int): Double =
    (this * 10.0.pow(decimals)).roundToInt() / 10.0.pow(decimals)

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var newPokemon by remember { mutableStateOf<String?>(null) }
    var newResets by remember { mutableStateOf(0) }
    var resetsAreInvalid by remember { mutableStateOf(false) }

    var currentPokemon by remember { mutableStateOf("") }
    var currentResets by remember { mutableStateOf(0) }

    var canAdvance by remember { mutableStateOf(true) }

    val params = CreateParams()
    params.clientID = 882770158958022747L
    params.flags = CreateParams.getDefaultFlags()

    val core = Core(params)
    val activity = Activity()

    activity.details = "Idling"

    core.activityManager().updateActivity(activity)

    Window(
        onCloseRequest = {
            activity.close()
            core.close()
            params.close()

            exitApplication()
        },
        title = "Shiny Tracker",
        onPreviewKeyEvent = {
            if (it.key != Key.Spacebar) return@Window false
            if (it.type != KeyEventType.KeyDown || !canAdvance) return@Window false

            currentResets++
            canAdvance = false

            thread {
                runBlocking {
                    delay(5000)
                    canAdvance = true
                }
            }

            return@Window true
        }
    ) {
        MaterialTheme(
            colors = darkColors()
        ) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                            .wrapContentHeight()
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(.3f).padding(end = 10.dp)
                                .height(50.dp),
                            value = newPokemon ?: "",
                            onValueChange = { newPokemon = it },
                            label = { Text("Pokemon Name") }
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth(.2f).padding(end = 10.dp)
                                .height(50.dp),
                            value = newResets.toString(),
                            onValueChange = {
                                val tentativeResets = it.toIntOrNull()

                                resetsAreInvalid = tentativeResets == null

                                if (!resetsAreInvalid)
                                    newResets = tentativeResets as Int
                            },
                            label = { Text("Resets") },
                            isError = resetsAreInvalid
                        )

                        Button(
                            modifier = Modifier.fillMaxWidth(.25f)
                                .height(50.dp),
                            onClick = {
                                if (resetsAreInvalid) return@Button

                                currentPokemon = newPokemon ?: return@Button
                                currentResets = newResets
                            }
                        ) {
                            Text("Start run")
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().focusable(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentResets.toString(),
                                fontSize = 10.em
                            )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentPokemon != "")
                                    "Chance for next $currentPokemon to be shiny: " +
                                        ((1.0 - (4095.0 / 4096.0).pow(currentResets)) * 100.0)
                                            .round(1) + "%"
                                    else "",
                                fontSize = 2.em
                            )
                        }
                    }
                }
            }
        }
    }
}