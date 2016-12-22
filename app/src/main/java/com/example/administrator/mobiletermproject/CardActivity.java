package com.example.administrator.mobiletermproject;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import static android.content.ContentValues.TAG;
import static android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE;

// LibraryActivity에서 Add한 카드들을 파일로 데이터를 저장한 후를 가정합니다

public class CardActivity extends AppCompatActivity {
    String cardFileAddress = "/data/data/com.example.administrator.mobiletermproject/files/";

    protected FileControl FileIO;
    private int CARDEDIT_REQUEST_CODE = 2;
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    public int year, month, day, hour, minute;
    private int mYear, mMonth, mDay, mHour, mMinute;
    protected TextView viewTitle, viewContent, showPathOfCard; // ex) library xxx in folder yyy
    private String title, folder, dirPathOfCard, nameOfLib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // 뒤로가기 버튼 만들기
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        FileIO = new FileControl(this);

        // 이전 인텐트를 이용해 값을 넘겨받음
        Intent intent = getIntent();
        title = intent.getStringExtra("Card_Title"); // 카드 제목 저장
        folder = intent.getStringExtra("Card_FIle_Folder"); // 해당 폴더 지정
        nameOfLib = intent.getStringExtra("Library_Title"); // 라이브러리 타이틀 지정

        // setTitle(title); // 카드 제목으로 액션바 제목 변경

        // 일단 액션바 제목 공란
        setTitle(" ");

        // 카드 내용을 저장하는 path 지정
        dirPathOfCard = cardFileAddress + nameOfLib + "/" + folder + "/" + title + ".txt";

        // 레이아웃 부르기
        viewTitle = (TextView)findViewById(R.id.cardTextTitle);
        viewContent = (TextView)findViewById(R.id.cardTextContent);
        showPathOfCard = (TextView)findViewById(R.id.pathOfCard);


        // 텍스트뷰 설정(setText), 메모 내용이 담긴 파일 로드(load)
        viewTitle.setText(title);
        viewContent.setText(FileIO.load(dirPathOfCard)); // 카드 내용을 바로 viewContent 에 set
        viewContent.setGravity(0); //글자 왼쪽 위부터 시작
        showPathOfCard.setText("[ " + folder + " ]" + " in Library < " + nameOfLib+ " >");


        // alarmSetting(); // 알람을 세팅하는 모듈
    }

    //CardEditActivity 로 부터의 인텐트 결과 받아오는 연산
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CARDEDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String newTitle = data.getStringExtra("data_title"); // 새로운 제목 데이터

                // 새로운 파일경로를 얻는다.
                dirPathOfCard = cardFileAddress
                        + nameOfLib + "/" + folder + "/" + newTitle + ".txt";

                // 새로운 제목으로 지정
                title = newTitle;
                viewTitle.setText(title);

                // 새로운 파일을 호출(load), 메모 내용에 저장(setText)
                viewContent.setText("");
                viewContent.setText(FileIO.load(dirPathOfCard));
            }
        }
    }

    // 메뉴생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //메뉴 리소스를 팽창
        getMenuInflater().inflate(R.menu.menu_card, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // 버튼 눌림 이벤트 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                break;

            case R.id.cardEdit:
                // 데이터들을 전달하려고 함
                Intent intentEdit = new Intent(this, CardEditActivity.class);
                intentEdit.putExtra("TITLE_EDIT", title);
                intentEdit.putExtra("Library_Name", nameOfLib);
                intentEdit.putExtra("Card_FIle_Folder", folder);

                //카드 이름을 전달
                startActivityForResult(intentEdit, CARDEDIT_REQUEST_CODE);
                break;

            case R.id.cardDel:
                // 카드 삭제 다이얼로그 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure to delete this card?")
                        .setCancelable(true)
                        // 확인 버튼 누를 시
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // 카드 삭제 구현
                                Intent intent = new Intent(CardActivity.this, LibraryActivity.class);
                                intent.putExtra("delete_title", title);

                                // 카드가 담긴 파일 일단 삭제해봄
                                FileIO.delete(dirPathOfCard);
                                releaseAlarm(getApplicationContext());
                                //Toast.makeText(getApplicationContext(), "카드 삭제 완료!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        // 취소 버튼 누를 시
                        .setNegativeButton("NO", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int whichButton){
                                dialog.cancel();
                                //Toast.makeText(getApplicationContext(), "카드 삭제 취소!", Toast.LENGTH_SHORT).show();
                            }
                        });

                AlertDialog dialog = builder.create(); // 알림창 객체 생성
                dialog.show(); // 알림창 띄우기
                break;

            case R.id.cardAlm:
                firstDateSetting(); // 현재날짜와 시간으로 세팅하는 모듈
                // Data Dialog 를 띄운다. 순차적으로 Time Dialog 도 띄운다.
                showDialog(DATE_DIALOG_ID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void firstDateSetting() {
        // 현재 시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
        // 현재 시간을 저장 한다.
        Date date = new Date(now);
        // 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
        String strNow = sdfNow.format(date);
        StringTokenizer tokens = new StringTokenizer(strNow);

        String yyyy = tokens.nextToken("/");
        String mm = tokens.nextToken("/");
        String dd = tokens.nextToken("/");
        String h = tokens.nextToken("/");
        String m = tokens.nextToken("/");

        mYear = Integer.parseInt(yyyy);
        mMonth =Integer.parseInt(mm)-1;
        mDay = Integer.parseInt(dd);
        mHour = Integer.parseInt(h);
        mMinute = Integer.parseInt(m);
    }


    // Time Dialog 생성 연산
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID : // 날짜 정보를 받는 경우
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
            case TIME_DIALOG_ID : // 시간 정보를 받는 경우
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

                    // 알람 설정에 대한 예외처리, 1
                    // 1. 입력받은 년도가 현재 년도보다 작은 경우
                    // 2. 같은 년도일떄 입력받은 월이 현재 월보다 작은 경우
                    // 3. 같은 년도, 같은 월일 때 입력받은 일이 현재 일보다 작은 경우
                    if (year < mYear ) {
                        Toast.makeText(getApplicationContext(),
                                "알람을 과거로 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    else if ( (year == mYear) && (month < mMonth) ) {
                        Toast.makeText(getApplicationContext(),
                                "알람을 과거로 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    else if ( (year == mYear) && (month == mMonth) && (day < mDay) ) {
                        Toast.makeText(getApplicationContext(),
                                "알람을 과거로 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    // DATE 다시 안띄워짐
                    // showDialog(DATE_DIALOG_ID);

                    else {
                        //Toast.makeText(getApplicationContext(), + year + " 년 " + (month+1) + " 월 " + day + " 일! ", Toast.LENGTH_SHORT).show();


                        // 날짜 선택 후 시간선택을 바로 할 수 있도록 한다
                        showDialog(TIME_DIALOG_ID);
                    }
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                    hour = hourOfDay;
                    minute = min;

                    // 알람 설정에 대한 예외처리, 2
                    // 1. 입력받은 시간이 현재 시간 보다 작은 경우
                    // 2. 같은 시간일떄 입력받은 분이 현재 분 보다 작은 경우
                    if (hour < mHour) {
                        Toast.makeText(getApplicationContext(),
                                "알람을 과거로 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    else if ( (hour == mHour) && (minute < mMinute) ) {
                        Toast.makeText(getApplicationContext(),
                                "알람을 과거로 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    // showDialog(TIME_DIALOG_ID);
                    else {
                        Toast.makeText(getApplicationContext(),
                                year + "년 " + (month+1) + "월 " + day + "일 " + hour + "시 " + minute + "분으로 알람이 설정되었습니다." , Toast.LENGTH_SHORT).show();

                        // 알람을 설정
                        setAlarm(getApplicationContext());
                    }
                }
            };

    // 값 불러오기
    private int getPreferences(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getInt(key, -1); //key값이 없다면 -1 반환
     }

    // 값 저장하기
    private void indexing(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        int newCode = getPreferences("index");
        if(newCode == -1)
            System.out.println("index key error!!");
        else {
            releaseAlarm(getApplicationContext());
            editor.remove(key);
            editor.putInt(key, newCode++);
            //index key값을 하나 증가 시킴
            editor.remove("index");
            editor.putInt("index", newCode);
        }
        editor.commit();
    }
    // 알람 해제
    private void releaseAlarm(Context context){
        Log.i(TAG, "releaseAlarm()");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(CardActivity.this, BroadcastAlm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(CardActivity.this, getPreferences(dirPathOfCard), intent, 0);
        alarmManager.cancel(pIntent);
    }

    private void setAlarm(Context context) {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        // AlarmManager는 device 에 미래에 대한 알림같은 행위를 등록할 때 사용
        Intent intent = new Intent(CardActivity.this, BroadcastAlm.class);

        //경로를 키값으로 리퀘스트 코드 인덱싱
        indexing(dirPathOfCard);

        intent.putExtra("data_title_for_push", title);
        intent.putExtra("data_folder_name", folder);
        intent.putExtra("data_library_name", nameOfLib);
        intent.putExtra("data_contents_for_push", viewContent.getText().toString());
        // 알람이 발생했을 경우, BroadcastAlm에게 방송을 해주기 위해서 명시적으로 알려줍니다.
        PendingIntent sender = PendingIntent.getBroadcast(CardActivity.this, getPreferences(dirPathOfCard), intent, 0);
        Calendar almCalendar = Calendar.getInstance();
        almCalendar.set(year, month, day, hour, minute); // 알람시간을 세팅해 줍니다.

        Log.i(TAG,  hour + " 시 " + minute + " 분에 알람이 울립니다! " );
        am.set(AlarmManager.RTC_WAKEUP, almCalendar.getTimeInMillis(), sender);
        // 알람을 세팅합니다.
        almCalendar.set(year, month, day, 0, 0);// 알람 세팅 이후에 연, 월, 일을 초기화 시켜줍니다.


    }

    public void onTestBtnListener(View v) {
        switch (v.getId()) {
            case R.id.testbtn:
                releaseAlarm(getApplicationContext());
        }
    }
}