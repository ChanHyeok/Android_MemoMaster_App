package com.example.administrator.mobiletermproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView library_listView;//라이브러리를 나열할 리스트 뷰
    private ArrayAdapter<String> library_Adapter; //라이브러리 리스트뷰에 쓰일 어댑터
    ///data/data/com.example.administrator.mobiletermproject/files/ 경로아래의 폴더명을 저장할 ArrayList
    private ArrayList<String> nameOfLib= new ArrayList<String>();
    private File filesPath = new File("/data/data/com.example.administrator.mobiletermproject/files"); //라이브러리(폴더)가 저장되는 경로


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //폴더에 있는 폴더들의 이름을 받아와 ArrayList에 초기화
        updateNOLList();
        //라이브러리 리스트뷰 호출
        getLibraryListView();

        //오른쪽 아래 버튼
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() { /////// + 버튼 눌렀을 때
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(MainActivity.this);
                input.setTextSize(20);
                input.setPadding(50,20,50,20);
                input.setHint("Type Library name");
                final AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setView(input)
                        .setTitle("New Library")
                        .setNegativeButton("CREATE", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton){

                            }
                        })
                        .setPositiveButton("CANCLE",new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton){

                                    }
                        }).create();

                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button Create = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                        Create.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                String value = input.getText().toString();
                                if(value.equals("")){ // 공백이면 제목적으라고 토스트 날려줌
                                    Toast.makeText(getApplicationContext(), "Library name can't be empty space", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else{
                                    int Llength = fileList().length; // 라이브러리 갯수 저장하는 변수다
                                    if(Llength != 0){
                                        boolean isAlreadyLibrary = false;
                                        String [] sLibraryList = fileList();

                                        for(int i=0; i< Llength; i++){
                                            if(sLibraryList[i].equals(value)){
                                                isAlreadyLibrary = true;
                                                break;
                                            }
                                        }
                                        if(isAlreadyLibrary){
                                            Toast.makeText(getApplicationContext(), "Already same Library name exist", Toast.LENGTH_SHORT).show();
                                            return; //리턴 시켜서 다시 작성하도록 권함
                                        }
                                        else{
                                            value.toString();
                                            // Do something with value!
                                            CreateDirectory(value);
                                            onResume();
                                        }
                                    }
                                    else{
                                        value.toString();
                                        // Do something with value!
                                        CreateDirectory(value);
                                        onResume();
                                    }
                                }
                                //Dismiss once everything is OK.
                                alert.dismiss();
                            }
                        });
                    }
                });
                alert.show();
        };

        });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(library_listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }// 슬라이드 했다가 조루처럼 놨을 때 ->신기한 기능이네

                            @Override
                            public void onDismiss(final ListView listView, final int[] reverseSortedPositions) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                                alert.setTitle("Library Deletion");
                                alert.setMessage("This Library will be disappear. Are you sure??");

                                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//yes 클릭 시
                                        //삭제
                                        for (int position : reverseSortedPositions) {
                                            //라이브러리 명으로 삭제 메서드 접근
                                            DeleteDir("/data/data/com.example.administrator.mobiletermproject/files/"+listView.getItemAtPosition(position));
                                            onResume();
                                        }
                                        //리스트뷰를 갱신하는 함수
                                        library_Adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });

                                alert.setNegativeButton("NO",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Toast.makeText(getApplicationContext(), "No Clicked", Toast.LENGTH_SHORT).show();
                                                // Canceled.
                                            }
                                        });
                                alert.show();
                            }
                        });
        library_listView.setOnTouchListener(touchListener);
        library_listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    /*
    디렉토리 및 하위디렉토리와 파일까지 싸그리 날리는 메소드
     */
    void DeleteDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            }
            else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //메인 액티비티로 복귀할 때 리스트뷰 업데이트를 위해 onResume()에 해당 코드를 삽입
    @Override
    protected void onResume() {
        //폴더에 있는 폴더들의 이름을 받아와 ArrayList에 초기화
        updateNOLList();
        //라이브러리 리스트뷰 호출
        getLibraryListView();
        super.onResume();
    }




    /*
   라이브러리 추가 메소드(폴더 생성)
    */
    private void CreateDirectory(String name){
        File file = new File( "/data/data/com.example.administrator.mobiletermproject/files/" + name);
        boolean success = true;
        if( !file.exists() ) {
            File folder1 = new File( "/data/data/com.example.administrator.mobiletermproject/files/" + name+ "/To Do");
            File folder2 = new File( "/data/data/com.example.administrator.mobiletermproject/files/" + name+ "/Progress");
            File folder3 = new File( "/data/data/com.example.administrator.mobiletermproject/files/" + name+ "/Done");
            File folder4 = new File( "/data/data/com.example.administrator.mobiletermproject/files/" + name+ "/IceBox");
            //mkdir은 상위디렉토리가 없다면 해당 폴더 생성안함.
            success = file.mkdirs();
            folder1.mkdir();
            folder2.mkdir();
            folder3.mkdir();
            folder4.mkdir();
        }
        if(success){
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    라이브러리에 쓰일 리스트뷰 연결 및 초기화
     */
    private void getLibraryListView(){
        // Android에서 제공하는 String 문자열 하나를 출력하는 layout으로 어댑터 생성
        library_Adapter = new ArrayAdapter<String>(this, R.layout.listview_items, nameOfLib);
        // Xml에서 추가한 ListView의 객체를 사용
        library_listView = (ListView) findViewById(R.id.list);
        library_listView.setAdapter(library_Adapter);
        // ListView 아이템 터치 시 이벤트를 처리할 리스너 설정
        library_listView.setOnItemClickListener(onClickListItem);

        //리스트뷰 속성
        library_listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);//화면을 넘어가면 스크롤바 생성
        library_listView.setDivider((new ColorDrawable(Color.GRAY)));//구분선 회색
        library_listView.setDividerHeight(5);//구분선 굵기
    }

    //화면 회전 시 onCreate 호출 방지를 위한 설정
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    /*
    라이브러리(폴더)이름을 저장하는 nameOfLib 리스트 업데이트
     */
    private void updateNOLList(){
        try{
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {//파일 필터. 폴더 만 읽어옴
                    return file.isDirectory();
                }
            };
            File[] files=filesPath.listFiles(directoryFilter); //필터에 걸러진 폴더들을 저장

            nameOfLib.clear();//arraylist 초기화

            System.out.println("    <<  라이브러리 목록  >>    ");
            System.out.println("다음과 같은 라이브러리가 있습니다.");
            if(files.length==0)System.out.println("Nothing to show");

            //instant-run 이거 이해안가네 리얼
            for(int i=1; i<=files.length; i++){  //i=0 부터하면 0에 instant-run 박혀있어서 별로안좋음
                nameOfLib.add(files[i].getName());
                System.out.println(i+" : "+files[i].getName());
            }
            return ;
        }

        catch( Exception e ){
            return ;
        }
    }


    /*
    액션바 메뉴 초기화
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    액션바 메뉴 리스너
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.Search){//검색 버튼 클릭시
            Toast.makeText(this, "검색 Activity로 이동", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()== R.id.Notipication){//알람 버튼 클릭시
            Toast.makeText(this, "알람 Activity로 이동", Toast.LENGTH_SHORT).show();
        }
        ////////////////// 이거 아직 지우지 말아보셈 (정민 )
        /*if(item.getItemId()== R.id.Delete){//삭제 버튼 클릭시
            Toast.makeText(this, "슬라이드하여 라이브러리를 삭제하세요.", Toast.LENGTH_LONG).show();
            SwipeDismissListViewTouchListener touchListener =
                    new SwipeDismissListViewTouchListener(library_listView,
                            new SwipeDismissListViewTouchListener.DismissCallbacks() {
                                @Override
                                public boolean canDismiss(int position) {
                                    return true;
                                }

                                @Override
                                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {
                                        //library_Adapter.remove(library_Adapter.getItem(position));
                                        deleteFile(nameOfLib.get(position));
                                        onResume();
                                    }
                                    library_Adapter.notifyDataSetChanged();
                                }
                            });
            library_listView.setOnTouchListener(touchListener);
            library_listView.setOnScrollListener(touchListener.makeScrollListener());
            // 한번 Delete가 선택 된 이후로 계속해서 슬라이드 하면 지워지는데
            // 이걸 막을 아이디어를 생각해야된다. 자기가 원할 때 멈출방법
            //fab 버튼으로 해볼려고했는데 잘안되네
        }*/
        return super.onOptionsItemSelected(item);
    }

    /*
    리스트뷰 이벤트 리스너
     */
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
            //리스트 객체 터치 시, LibraryActivity로 터치한 리스트아이템명을 보냄
            intent.putExtra("library_name",library_Adapter.getItem(position));
            startActivity(intent);
        }
    };
}
