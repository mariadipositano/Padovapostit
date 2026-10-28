package it.padovapostit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlin.random.Random

/** Widget 4x4: doppio tap su un post-it = compare/scompare un disegno casuale di Padova. */
class PostitWidget : AppWidgetProvider() {

    private val cells = intArrayOf(R.id.cell_0, R.id.cell_1, R.id.cell_2, R.id.cell_3, R.id.cell_4, R.id.cell_5, R.id.cell_6, R.id.cell_7, R.id.cell_8, R.id.cell_9, R.id.cell_10, R.id.cell_11, R.id.cell_12, R.id.cell_13, R.id.cell_14, R.id.cell_15)
    private val drawings = intArrayOf(R.drawable.d_basilica, R.drawable.d_torre_orologio, R.drawable.d_prato_valle, R.drawable.d_palazzo_ragione, R.drawable.d_pedrocchi, R.drawable.d_orto_botanico, R.drawable.d_ponte, R.drawable.d_santa_giustina, R.drawable.d_specola_frontale, R.drawable.d_specola_stelle, R.drawable.d_specola_telescopio, R.drawable.d_specola_prospettiva)
    private val doubleTapMs = 400L

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, widgetIds: IntArray) = refresh(ctx, mgr, widgetIds)

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action != ACTION_TAP) return
        val i = intent.getIntExtra("cell", -1)
        if (i !in cells.indices) return
        val p = ctx.getSharedPreferences("postit", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        if (now - p.getLong("last_$i", 0L) <= doubleTapMs) {
            val shown = p.getInt("img_$i", -1)
            p.edit().putInt("img_$i", if (shown == -1) drawings[Random.nextInt(drawings.size)] else -1)
                .putLong("last_$i", 0L).apply()
        } else p.edit().putLong("last_$i", now).apply()
        val mgr = AppWidgetManager.getInstance(ctx)
        refresh(ctx, mgr, mgr.getAppWidgetIds(ComponentName(ctx, PostitWidget::class.java)))
    }

    private fun refresh(ctx: Context, mgr: AppWidgetManager, widgetIds: IntArray) {
        val p = ctx.getSharedPreferences("postit", Context.MODE_PRIVATE)
        val rv = RemoteViews(ctx.packageName, R.layout.widget_postit)
        for (i in cells.indices) {
            val img = p.getInt("img_$i", -1)
            if (img == -1) rv.setImageViewResource(cells[i], 0) else rv.setImageViewResource(cells[i], img)
            val intent = Intent(ctx, PostitWidget::class.java).setAction(ACTION_TAP).putExtra("cell", i)
            val pi = PendingIntent.getBroadcast(ctx, i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            rv.setOnClickPendingIntent(cells[i], pi)
        }
        widgetIds.forEach { mgr.updateAppWidget(it, rv) }
    }

    companion object { const val ACTION_TAP = "it.padovapostit.TAP" }
}
