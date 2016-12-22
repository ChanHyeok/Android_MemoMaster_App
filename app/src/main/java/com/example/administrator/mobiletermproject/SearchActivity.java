package com.example.administrator.mobiletermproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * Created by Administrator on 2016-11-12.
 */
public class SearchActivity extends AppCompatActivity {
    private TextWatcher watcher;//EditText를 실시간으로 감시
    private EditText search;//검색창에 쓰일 뷰
    private ListView search_listView;//검색한 것과 일치하는 것을 나열할 리스트 뷰
    private ArrayAdapter<String> search_Adapter; //서치 리스트뷰에 쓰일 어댑터
    private ArrayList<String> nameOfCard= new ArrayList<String>();

    //뒤로버튼 클릭 시
    public void onBackbtnClicked(View view){
        finish();
    }
    //X버튼 클릭 시
    public void onCancelClicked(View view){
        search.setText(""); //지금까지 썻던거 없애주려고 만들었음
    }

    @Override
    public void onBackPressed() {
        System.out.println("백버튼 눌림");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);

        /*
        커스텀 액션바 ( 검색 창 )
        */
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.search_custom_actionbar);//커스텀뷰 연결

        search = (EditText) actionBar.getCustomView().findViewById(R.id.searchfield);

        //텍스트값 수정시 마다 호출되는 이벤트 리스너
        watcher= new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchActivity.this.search_Adapter.getFilter().filter(s);  //이부분이 글자 치는 거 대로 필터링해주는부분
              //  Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        search.addTextChangedListener(watcher);//텍스트를 계속 봄
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        actionBar.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM | android.support.v7.app.ActionBar.DISPLAY_SHOW_HOME);

    }

    @Override
    protected void onResume() {
        //라이브러리 리스트뷰 호출
        getAllCardName();
        getSearchListView();
        super.onResume();
    }

    /*
   서치카드에 쓰일 리스트뷰 연결 및 초기화
    */
    private void getSearchListView(){
        // Android에서 제공하는 String 문자열 하나를 출력하는 layout으로 어댑터 생성
        search_Adapter = new ArrayAdapter<String>(this, R.layout.search_list_items, R.id.product_name, nameOfCard);
        // Xml에서 추가한 ListView의 객체를 사용
        search_listView = (ListView) findViewById(R.id.search_list);
        search_listView.setAdapter(search_Adapter);
        // ListView 아이템 터치 시 이벤트를 처리할 리스너 설정
        search_listView.setOnItemClickListener(onClickListItem);

        //리스트뷰 속성
        search_listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);//화면을 넘어가면 스크롤바 생성
        search_listView.setDivider((new ColorDrawable(Color.GRAY)));//구분선 회색
        search_listView.setDividerHeight(5);//구분선 굵기
    }

    /*
    리스트뷰 이벤트 리스너
     */
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(SearchActivity.this, CardActivity.class);
            //리스트 객체 터치 시, CardAct로 터치한 리스트아이템명을 보냄

            String txt = parent.getItemAtPosition(position).toString();
            // StringTokenizer를 이용한 문자열 분리
            StringTokenizer tokens = new StringTokenizer(txt);

            String cardTitle = tokens.nextToken("\n") ;   // 이름
            String garbage = tokens.nextToken("/") ;    //안쓰는거
            String libraryTitle = tokens.nextToken("/") ;   //라이브러리 이름
            String folderTitle = tokens.nextToken("/") ;

            intent.putExtra("Card_Title",cardTitle);
            intent.putExtra("Library_Title",libraryTitle);
            intent.putExtra("Card_FIle_Folder",folderTitle);

            startActivity(intent);

        }
    };

    //카드 목록 전부다 가져오는 함수
    public void getAllCardName(){
        File  file = new File("/data/data/com.example.administrator.mobiletermproject/files");
        File [] files = file.listFiles();
        nameOfCard.clear();
        for(int i=1; i<file.listFiles().length; i++){ //인스턴트 런생각해서
            File  folder = new File("/data/data/com.example.administrator.mobiletermproject/files/" + files[i].getName());
            File [] folderfiles = folder.listFiles();
            for(int j=0; j<folder.listFiles().length; j++){
                File card = new File("/data/data/com.example.administrator.mobiletermproject/files/" + files[i].getName() +"/" + folderfiles[j].getName());
                File [] cardList = card.listFiles();
                for(int k=0; k<card.listFiles().length;k++)
                    nameOfCard.add(cardList[k].getName().substring(0,cardList[k].getName().length()-4) +"\n" +"-> /" +files[i].getName()+"/"+folderfiles[j].getName());

            }
        }

    }

}
