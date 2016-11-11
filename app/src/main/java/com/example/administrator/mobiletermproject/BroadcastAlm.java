package com.example.administrator.mobiletermproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by wooyo on 2016-11-11.
 */
// 알림 기능 아직 계속 삽질 중, 미완성

public class BroadcastAlm extends BroadcastReceiver {
    // String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;

    // 알람 시간이 되었을때 onReceive를 호출함
    @Override
    public void onReceive(Context context, Intent intent) {
        //NotificationManager 안드로이드 상태바에 메세지를 던지기위한 서비스 불러오고
        NotificationManager notificationmanager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(context, 0, new Intent(context, CardActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker")                  // 알림이 출력될 때 상단에 나오는 문구.
                .setWhen(System.currentTimeMillis())  // 알람 울릴 시간
                .setNumber(1).setContentTitle("받아올 Title")
                .setContentText("받아올 Content 의 일부")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) // 알람 형태(진동, 소리)
                .setContentIntent(pendingIntent)       // 알림 터치시 반응
                .setAutoCancel(true);                  // 알람 터치시 반응 후 삭제 여부

        // System.currentTimeMillis :: 시스템 현재시각 불러오는 함수
        notificationmanager.notify(1, builder.build());
    }
}