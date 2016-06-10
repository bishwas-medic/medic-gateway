package medic.gateway.alert;

import android.app.*;
import android.content.*;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import static medic.gateway.alert.GatewayLog.*;

public class AlarmListener implements WakefulIntentService.AlarmListener {
	public void scheduleAlarms(AlarmManager am, PendingIntent pendingIntent, Context ctx) {
		if(Settings.in(ctx).isPollingEnabled()) {
			logEvent(ctx, "AlarmManager.scheduleAlarms() :: polling enabled - setting alarms");
			am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), getPollInterval(ctx), pendingIntent);
		} else {
			logEvent(ctx, "AlarmManager.scheduleAlarms() :: polling disabled - cancelling alarms");
			WakefulIntentService.cancelAlarms(ctx);
		}
	}

	public void sendWakefulWork(Context ctx) {
		WakefulIntentService.sendWakefulWork(ctx, new Intent(ctx, WakefulService.class));
	}

	public long getMaxAge(Context ctx) {
		return getPollInterval(ctx) * 2L;
	}

//> PUBLIC STATIC
	public static void restart(Context ctx) {
		WakefulIntentService.cancelAlarms(ctx);
		WakefulIntentService.scheduleAlarms(new AlarmListener(), ctx);
	}

//> STATIC HELPERS
	private static long getPollInterval(Context ctx) {
		return Settings.in(ctx).getPollInterval();
	}
}