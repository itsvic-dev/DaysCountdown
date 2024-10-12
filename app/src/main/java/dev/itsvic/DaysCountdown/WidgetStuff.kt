package dev.itsvic.DaysCountdown

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyAppWidget()
}

class MyAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        context.startTimeSyncWorker()
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val dateMillis by context.settingsDataStore.data.map { it.dateMillis }.collectAsState(null)
        val daysUntil = dateMillis?.let {
            if (it == 0.toLong()) "" else daysUntilNow(it)
        } ?: ""

        Box(modifier = GlanceModifier.fillMaxSize()) {
            Image(
                ImageProvider(R.drawable.widget_pill_shape),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.surface),
                contentDescription = "",
                modifier = GlanceModifier.fillMaxSize()
            )

            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$daysUntil",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
                Text(
                    "days left", style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                    )
                )
            }
        }
    }
}

class TimeSyncWorker(
    private val context: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        MyAppWidget().updateAll(context)
        return Result.success()
    }
}

fun Context.startTimeSyncWorker() {
    val request = PeriodicWorkRequest.Builder(TimeSyncWorker::class.java, 1, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            "WidgetTimeSyncWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
}
