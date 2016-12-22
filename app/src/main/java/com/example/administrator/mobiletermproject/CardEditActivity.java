package com.example.administrator.mobiletermproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class CardEditActivity extends AppCompatActivity implements View.OnClickListener {
    String cardFileAddress = "/data/data/com.example.administrator.mobiletermproject/files/";
    FileControl FileIO;

    Button editComplete;

    private String title = ""; // 카드 제목
    private String dirPathOfCard, nameOfLib, folder; // 카드 경로, 라이브러리 이름, 폴더이름

    protected TextView showPathOfCard;
    protected EditText editTitle, actionbarCardName, editContent; // ex) library xxx in folder yyy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardedit);

        // 뷰 초기화
        editTitle = (EditText)findViewById(R.id.cardEditTextTitle);
        editContent = (EditText)findViewById(R.id.cardTextContent);

        showPathOfCard=(TextView)findViewById(R.id.pathOfCard);
        editComplete = (Button)findViewById(R.id.editCompelete);

        CardToEditIntent(); // 인텐트 값 받아오는 메소드

        showPathOfCard.setText("[ " + folder + " ]" +" in Library < " + nameOfLib+ " >");
        // 경로 출력

        /*
        커스텀 액션바 ( 카드 제목 수정 )
        */
        // android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // actionBar.setCustomView(R.layout.editcard_custom_actionbar); // 커스텀뷰 연결
        // actionBar.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);

        // 액션바의 카드이름을 출력 및 수정 가능하게 하는 editText
        // actionbarCardName = (EditText) actionBar.getCustomView().findViewById(R.id.cardName);
        // actionbarCardName.setText(title);
        setTitle(" ");

        dirPathOfCard = cardFileAddress + nameOfLib + "/" + folder + "/" + title + ".txt";

        // 그냥 editText 하나 만들어서 여기에서 카드이름 출력, 수정하게끔 하였음
        editTitle.setText(title);

        FileIO = new FileControl(this);
        editContent.setText(FileIO.load(dirPathOfCard));

        // 버튼 누름시 CardActivity 로 이동함
        editComplete.setOnClickListener(this);
    }

    public void CardToEditIntent() {
        //이전 인텐트를 이용해 값을 넘겨받음
        Intent intentEdit = getIntent();

        folder = intentEdit.getStringExtra("Card_FIle_Folder"); // 폴더 저장
        nameOfLib = intentEdit.getStringExtra("Library_Name"); // 라이브러리 저장
        title = intentEdit.getStringExtra("TITLE_EDIT"); // 타이틀 저장
    }
    /*
    버튼 클릭 리스너
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editCompelete:
                // 변경된 타이틀과 비교해서 같으면 파일 구지 삭제할 필요가 없음
                // 수정된 editTitle 의 string 을 긁어옴
                String changedTitle = editTitle.getText().toString();
                if ( changedTitle.compareTo("") == 0 ) { // 수정한 메모 제목이 null일 경우
                    Toast.makeText(getApplicationContext(),
                            "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }

                else { // 정상적으로 수정을 시도하려는 경우
                    if (title.compareTo(changedTitle) != 0) { // 두 파일 이름이 같지 않으면
                        String changedFile = cardFileAddress +
                                nameOfLib + "/" + folder + "/" + changedTitle + ".txt";

                        //같은 이름의 카드가 존재 한다면
                        File file = new File(changedFile);
                        if (file.isFile()) {
                            Intent intent = new Intent(this, CardActivity.class);
                            intent.putExtra("data_title", title);

                            Toast.makeText(getApplicationContext(),
                                    "같은 제목의 카드가 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, intent);
                            finish();
                            }

                        else {
                            // 바뀐 제목으로 파일 새로 생성
                            FileIO.save(editContent.getText().toString(), changedFile);

                            // 기존 파일 삭제
                            FileIO.delete(dirPathOfCard);

                            title = changedTitle; // 바뀐 제목을 title 에 재지정 한 후 넘겨주자
                            Intent intent = new Intent(this, CardActivity.class);
                            intent.putExtra("data_title", changedTitle);

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    // 파일 제목이 바뀌지 아니하였을 때
                    else {
                        // 삭제하구 다시 저장
                        FileIO.delete(dirPathOfCard);
                        FileIO.save(editContent.getText().toString(), dirPathOfCard);

                        Intent intent = new Intent(this, CardActivity.class);
                        intent.putExtra("data_title", changedTitle);

                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }
}
