package com.bibta.miti;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Pair;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

/**
 * Miti-Widget-Provider for 4x1 default widget.
 */
public class MitiWidgetProvider extends AppWidgetProvider {

    /**
     * Update widget with current date and time.
     * @param context Application widget context.
     * @param remoteViews Remote views containing the widget views.
     */
    public static void updateWidget(Context context, RemoteViews remoteViews) {
        Calendar calendar = Calendar.getInstance();

        // english date
        String engDate = calendar.get(Calendar.DAY_OF_MONTH)+"";
        remoteViews.setTextViewText(R.id.engDate, engDate);
        String engMonthYear = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
                + "\n" + calendar.get(Calendar.YEAR);
        remoteViews.setTextViewText(R.id.engMonthYear, engMonthYear);

        // day
        String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        remoteViews.setTextViewText(R.id.day, day);

        // nepali date
        Date nepDate = new Date(calendar).convertToNepali();
        String nepMonthYear = NepaliTranslator.getMonth(nepDate.month) + "\n" +
                NepaliTranslator.getNumber(nepDate.year + "");
        remoteViews.setTextViewText(R.id.nepMonthYear, nepMonthYear);
        remoteViews.setTextViewText(R.id.nepDate, NepaliTranslator.getNumber(nepDate.day + ""));

        // tithi
        String date_str = String.format("%04d-%02d-%02d", nepDate.year, nepDate.month, nepDate.day);
        Pair<String, String> tithi = new TithiDb(context).get(date_str);
        remoteViews.setTextViewText(R.id.tithi, tithi.first);


        // Set alarm to update in next minute
        int timeTillNextMinute = (60 - calendar.get(Calendar.SECOND)) * 1000;
        Intent intent = new Intent(context, MitiWidgetProvider.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
                + timeTillNextMinute, pendingIntent);
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        ComponentName thisWidget = new ComponentName(context, MitiWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_miti);
            updateWidget(context, remoteViews);

            // Register an onClickListener to launch MainActivity
            Intent intent1 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
