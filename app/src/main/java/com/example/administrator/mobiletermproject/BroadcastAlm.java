package com.example.administrator.mobiletermproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by wooyo on 2016-11-11.
 */

public class BroadcastAlm extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;
    @Override
    public void onReceive(Context context, Intent intent) {
        //알람 시간이 되었을때 onReceive를 호출함
         Log.i(TAG, "알람이 울립니다!!!");

        // Notify 할 데이터를 가져옴
        String titleTemp = intent.getStringExtra("data_title_for_push");
        String contentTemp = intent.getStringExtra("data_contents_for_push");


        // Notification 생성 (맨위에 뜰 푸쉬알람)
        NotificationManager notificationmanager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent broadToCard = new Intent(context, CardActivity.class);
        broadToCard.putExtra("Library_Title", intent.getStringExtra("data_library_name")); // 라이브러리 이름
        broadToCard.putExtra("Card_FIle_Folder", intent.getStringExtra("data_folder_name")); //폴더 이름
        broadToCard.putExtra("Card_Title", titleTemp); //카드 이름름
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, broadToCard, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Builder
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("")
                .setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle(titleTemp) // title 를 담음
                .setContentText(contentTemp) // content 를 담음
                .setDefaults(Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // 기기에(애뮬레이터에) Notify 함
        notificationmanager.notify(1, builder.build());

    }
}