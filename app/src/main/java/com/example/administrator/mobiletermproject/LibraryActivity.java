package com.example.administrator.mobiletermproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {
    private final int FOLDER_NUMBER=4;//폴더의 수 - 현재 변경 불가
    private int[] resID=new int[FOLDER_NUMBER];//폴더 각각의 리스트 아이디를 저장할 배열
    private int[] textID=new int[FOLDER_NUMBER];//폴더 각각의 제목(EditText)을 저장할 배열
    private ListView[] folder_ListView = new ListView[FOLDER_NUMBER];//카드를 나열할 리스트뷰의 배열
    private Folder[] folders = new Folder[FOLDER_NUMBER];//폴더 객체 생성
    private EditText[] folderTitle= new EditText[FOLDER_NUMBER];//폴더의 제목을 표시할 EditText의 배열
    private ArrayAdapter<String> folder_Adapter;//리스트뷰에 쓰일 어댑터
    private ArrayList<String> nameOfFolder= new ArrayList<String>();//폴더명을 저장할 ArrayList
    private int folderIndex=-1;//이벤트가 발생한 곳이 어느 폴더인지 저장하는 변수 (0~3 값 범위)
    private String title="";//현재 Library의 이름을 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(getApplicationContext(), "LibraryAct's onCreate called.", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        //메인액티비티로부터 라이브러리이름 데이터 받아옴
        Intent intent = getIntent();
        title=intent.getExtras().getString("library_name");

        //타이틀을 라이브러리 이름으로 변경
        setTitle(title);

        //라이브러리의 이름에 따라 nameOfFolder를 라이브러리 하위 디렉토리들의 이름으로 업데이트
        updateNOFList(title);

        //네개의 폴더와 각각의 리스트뷰, 제목을 초기화
        for(int j=0;j<FOLDER_NUMBER;j++) {
            resID[j]=getResources().getIdentifier("com.example.administrator.mobiletermproject:id/list" + j, null, null);
            textID[j]=getResources().getIdentifier("com.example.administrator.mobiletermproject:id/text" + j, null, null);
            folders[j]=new Folder(nameOfFolder.get(j), resID[j],textID[j], title);
            folderTitle[j]=(EditText) findViewById(folders[j].getTextId());
            folderTitle[j].setText(folders[j].getFolderName());

            // 폴더내의 카드들의 목록을 layout으로 출력하는 어댑터 생성
            folder_Adapter = new ArrayAdapter<String>(this, R.layout.library_listview_items, folders[j].nameOfCard);
            // activity_libraty.xml 의 리스트뷰 아이디를 읽어와서 연결
            folder_ListView[j] = (ListView) findViewById(folders[j].getlistVIewId());
            folder_ListView[j].setAdapter(folder_Adapter);
            //리스너
            folder_ListView[j].setOnItemClickListener(onClickListItem);

            //리스트뷰 속성
            folder_ListView[j].setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);//화면을 넘어가면 스크롤바 생성
            folder_ListView[j].setDivider((new ColorDrawable(Color.GRAY)));//구분선 회색
            folder_ListView[j].setDividerHeight(5);//구분선 굵기
        }
    }

    @Override
    protected void onResume() {
        //카드 리스트뷰 4개 초기화
        for(int j=0;j<FOLDER_NUMBER;j++) {
            resID[j]=getResources().getIdentifier("com.example.administrator.mobiletermproject:id/list" + j, null, null);
            textID[j]=getResources().getIdentifier("com.example.administrator.mobiletermproject:id/text" + j, null, null);
            folders[j]=new Folder(nameOfFolder.get(j), resID[j],textID[j], title);
            folderTitle[j]=(EditText) findViewById(folders[j].getTextId());
            folderTitle[j].setText(folders[j].getFolderName());

            // 폴더내의 카드들의 목록을 layout으로 출력하는 어댑터 생성
            folder_Adapter = new ArrayAdapter<String>(this, R.layout.library_listview_items, folders[j].nameOfCard);
            // activity_libraty.xml 의 리스트뷰 아이디를 읽어와서 연결
            folder_ListView[j] = (ListView) findViewById(folders[j].getlistVIewId());
            folder_ListView[j].setAdapter(folder_Adapter);
            //리스너
            folder_ListView[j].setOnItemClickListener(onClickListItem);

        }

        super.onResume();
    }

    //현재 클릭한 add버튼이 어느 카드리스트에 카드를 추가해야 할 것인지 folder 번호를 매기기 위해 사용하는 메서드 유가릿?
    private void setFolderIndex(View view){
        switch(view.getId()) {
            case R.id.add1:
                folderIndex=0;
                break;
            case R.id.add2:
                folderIndex=1;
                break;
            case R.id.add3:
                folderIndex=2;
                break;
            case R.id.add4:
                folderIndex=3;
                break;
        }
    }
    private int getFolderIndex(){
        return folderIndex;
    }

    /*
    카드 추가 버튼 이벤트 리스너
     */
    public void onCardAddbtnClicked(View view){
        setFolderIndex(view);//현재 눌린 버튼이 어느 폴더에 속하는지 판별하는 folderIndex를 초기화

        //다이얼로그 생성
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //relativelayout 생성 id:0
        RelativeLayout relativeLayout      = new RelativeLayout(this);
        relativeLayout.setId(0);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relativeParams.setMargins(20,20,20,20);
        relativeLayout.setLayoutParams(relativeParams);

        //텍스트뷰 1 id:1
        relativeParams=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        TextView cardNameBox        = new TextView(this);
        cardNameBox.setId(1);//에러아님
        cardNameBox.setPadding(40,40,40,20);
        cardNameBox.setTextSize(15);
        cardNameBox.setLayoutParams(relativeParams);
        cardNameBox.setText("Card Name");
        relativeLayout.addView(cardNameBox);

        //에딧텍스트 1 id:2
        relativeParams=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardNameBox.getId());
        final EditText cardNameInput    = new EditText(this);
        cardNameInput.setTextColor(Color.parseColor("#000000"));
        cardNameInput.setId(2);//에러아님
        cardNameInput.setPadding(40,0,40,50);
        cardNameInput.setTextSize(25);
        cardNameInput.setSingleLine();
        cardNameInput.setMaxLines(1);
        cardNameInput.setGravity(0);
        cardNameInput.setLayoutParams(relativeParams);
        relativeLayout.addView(cardNameInput);

        //텍스트뷰 2 id:3
        relativeParams=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardNameInput.getId());
        TextView cardContentBox        = new TextView(this);
        cardContentBox.setId(3);//에러아님
        cardContentBox.setPadding(40,30,40,20);
        cardContentBox.setTextSize(15);
        cardContentBox.setLayoutParams(relativeParams);
        cardContentBox.setText("Card Content");
        relativeLayout.addView(cardContentBox);

        //에딧텍스트 2 id:4
        relativeParams=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardContentBox.getId());
        final EditText cardContentInput    = new EditText(this);
        cardContentInput.setTextColor(Color.parseColor("#000000"));
        cardContentInput.setId(4);//에러아님
        cardContentInput.setPadding(40,0,40,40);
        cardContentInput.setTextSize(20);
        cardContentInput.setLines(3);
        cardContentInput.setMaxLines(3);
        cardContentInput.setGravity(0);
        cardContentInput.setLayoutParams(relativeParams);
        relativeLayout.addView(cardContentInput);

        alert.setTitle("");
        alert.setView(relativeLayout);

        //Save 버튼을 눌럿을 때
        alert.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                folderIndex=getFolderIndex();
                String strTitle=cardNameInput.getText().toString();//제목 저장
                String strContents=cardContentInput.getText().toString();//내용 문자열 저장
                if(strTitle.length()==0 || folders[getFolderIndex()].nameOfCard.contains(strTitle)){ //만약 입력된 제목이 공백이거나 중복이라면
                    Toast.makeText(getApplicationContext(), "Please check <Card Name> again. it can't be empty space or have duplicate name.", Toast.LENGTH_LONG).show();

                }
                else{//정상 실행
                    folders[getFolderIndex()].nameOfCard.add(strTitle);//리스트뷰에 표시할 arraylist 갱신
                    //파일 생성
                    try {
                        FileOutputStream fos = new FileOutputStream("/data/data/com.example.administrator.mobiletermproject/files/"+title+"/"+folders[getFolderIndex()].getFolderName()+"/"+strTitle+".txt",true);//제목+.txt 형식으로 파일 생성.
                        fos.write(strContents.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                onResume();
                dialog.cancel();
            }
        });

        //Cancel 버튼을 눌럿을 때
        alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //String name = etInput.getText().toString();
                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }


    /*
    카드를 담고있는 리스트뷰의 이벤트 리스너 구현
     */
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//리스트 객체 터치 시, SeeMemoActivity 실행 및 인텐트 결과값 반환받음
            switch (parent.getId()) {
                case R.id.list0://1번째 카드 리스트뷰
                    Toast.makeText(getApplicationContext( ), "Cardlist01가 눌려졌음다"+id, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.list1://2번째 카드 리스트뷰
                    Toast.makeText(getApplicationContext( ), "Cardlist02가 눌려졌음다"+id, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.list2://3번째 카드 리스트뷰
                    Toast.makeText(getApplicationContext( ), "Cardlist03가 눌려졌음다"+id, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.list3://4번째 카드 리스트뷰
                    Toast.makeText(getApplicationContext( ), "Cardlist04가 눌려졌음다"+id, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*
   입력받은 디렉토리 하위의 디렉토리들의 목록을 nameOfFolder에 업데이트
    */
    private void updateNOFList(String title){
        try{
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {//파일 필터. 폴더 만 읽어옴
                    return file.isDirectory();
                }
            };
            File filesPath = new File("/data/data/com.example.administrator.mobiletermproject/files/"+title);
            File[] files=filesPath.listFiles(directoryFilter); //필터에 걸러진 폴더들을 저장
            nameOfFolder.clear();//arraylist 초기화
            System.out.println("        <<  라이브러리 : "+title+"   >>        ");
            System.out.println("다음과 같은 하위 폴더들이 있습니다.");
            if(files.length==0)System.out.println("Nothing to show");
            for(int i=0; i<files.length; i++){
                nameOfFolder.add(files[i].getName());
                System.out.println(i+" : "+files[i].getName());
            }
            return ;
        }
        catch( Exception e ){
            return ;
        }
    }

    //메뉴생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_library, menu);
        return true;
    }

    //액션바의 메뉴 버튼 리스너
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.menuCreateMemo){//메모 생성 버튼 클릭시
            Intent intent = new Intent(MainActivity.this, CreateActivity.class);
            startActivityForResult(intent,1);
        }
        else if(item.getItemId()== R.id.menuDeleteAll){//전체 삭제 버튼 클릭시
            //경고 대화상자 생성
            final AlertDialog.Builder popup = new AlertDialog.Builder(MainActivity.this);
            popup.setTitle("CAUTION");
            popup.setMessage("All MEMOS will be delete. Are you sure?");
            popup.setPositiveButton("YES", new DialogInterface.OnClickListener() {//yes 선택시
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    while(nameOfFile.size()!=0){//arraylist가 빌 때 까지 파일삭제 및 arraylist 제거연산 수행
                        deleteFile(nameOfFile.get(0));
                        nameOfFile.remove(0);
                    }
                    dialog.dismiss();
                    onResume();//메인 엑티비티 업데이트
                }
            });
            popup.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {//no 선택시
                    //Toast.makeText(getApplicationContext(), "No Clicked", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
            popup.setIcon(R.drawable.alert);//대화창 아이콘 설정
            popup.create();
            popup.show();
        }
        return true;
    }
    */
}
