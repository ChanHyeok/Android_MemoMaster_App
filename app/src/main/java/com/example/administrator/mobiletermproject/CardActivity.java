package com.example.administrator.mobiletermproject;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.ContentValues.TAG;

// LibraryActivity에서 Add한 카드들을 파일로 데이터를 저장한 후를 가정합니다

public class CardActivity extends AppCompatActivity {
    Button btnDate, btnTime, btnPush;
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    public int year, month, day, hour, minute;
    private int mYear, mMonth, mDay, mHour, mMinute;

    EditText title, content;
    protected TextView cardRawPath; // ex) library xxx in folder yyy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        year = 2016; // 연도 초기화

        // 레이아웃 부르기
        title = (EditText)findViewById(R.id.cardTextTitle);
        content = (EditText)findViewById(R.id.cardTextContent);
        cardRawPath = (TextView)findViewById(R.id.textViewPath);

        // title, content는 TextView가 아니라 터치 시에 수정이 가능하게끔
        // EditText로 작성을 하였고, manifests 부분에
        // <activity> </> 사이에 android:windowSoftInputMode="adjustResize" 를 추가하므로써
        // 액티비티가 뜨면서 바로 키보드가 나타나지 않게 설정하였다.

        btnDate = (Button)findViewById(R.id.DateButton); // 날짜 설정 버튼
        btnTime = (Button)findViewById(R.id.TimeButton); // 시간 설정 버튼
        btnPush = (Button)findViewById(R.id.Pushbutton); // 푸시알람 설정 버튼

        // Button 눌림 시 Date, Time 설정을 화면에 표시하게 한다
        // Push 알람 설정을 완료하도록 한다.
        btnDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        btnPush.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "hour = " + String.valueOf(hour));
                new AlarmHATT(getApplicationContext()).Alarm();
            }
        });

    }

    // 카드에서 수정한 데이터들을 다시 파일에 저장하는 함수
    public void CardDataToFolder() {
        // LibraryActivity에서 Add한 카드들이 저장된 파일들을 불러와서

        // 따로 불러올 필요없이 여기서 수정한 editText들을
        // getText를 이용하여 다시 파일에 저장하도록
    }


    // 카드 액티비티에서 추가적인 기능들은 추후 구현합니다.
    // 알람기능, comment기능 등.. (기존 어플 참고)
    public void AddtionalFunction() {

    }

    // Time Dialog 생성 연산
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID :
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
            case TIME_DIALOG_ID :
                return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
        }
        return null;
    }

    // 사용자가 날짜를 선택하고 완료 버튼을 누르면 onDateSet()이 호출된다.
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int yearSelected,
                                      int monthOfYear, int dayOfMonth) {
                    year = yearSelected;
                    month = monthOfYear;
                    day = dayOfMonth;
                    Toast.makeText(getApplicationContext(),
                            "날짜 : " + year + " - " + month + " - " + day,
                            Toast.LENGTH_SHORT).show();
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                    hour = hourOfDay;
                    minute = min;
                    Toast.makeText(getApplicationContext(),
                            "시간 : " + hour + " - " + minute,
                            Toast.LENGTH_SHORT).show();
                }
            };

    public class AlarmHATT {
        private Context context;

        public AlarmHATT(Context context) {
            this.context = context;
        }
        public void Alarm() {
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            // AlarmManager는 device에 미래에 대한 알림같은 행위를 등록할 때 사용
            Intent intent = new Intent(CardActivity.this, BroadcastAlm.class);
            // 알람이 발생했을 경우, BradcastALm에게 방송을 해주기 위해서 명시적으로 알려주는것

            PendingIntent sender = PendingIntent.getBroadcast(CardActivity.this, 0, intent, 0);
            // MainActivity.this => PendingIntent를 부르려는 컨텍스트
            // int requestCode =>Private request code 인데 현재는 사용하지 않아서 0으로
            // Intent intent => 앞으로 불려질 Intent
            // int flags => Intent에 대한 조건설정 플래그
                // FLAG_ONE_SHOT : 한번만 사용하고 다음에 이 PendingIntent가 불려지면 fail 하겠다.
                // FLAG_NO_CREATE : PendingIntent를 생성하지 않는다. PendingIntent가 실행중인것을 체크하기위함
                // FLAG_CANCEL_CURRENT : 이미 실행중인 PendingIntent가 있다면 이를 취소하고 새로 만듬
                // FLAG_UPDATE_CURRENT : 이미 실행중인 PendingIntent가 있다면 새로 만들지않고  extra data 만 교체하겠다.

            Calendar calendar = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            calendar.set(year, month, day, hour, day);

            //알람 예약
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }

}
