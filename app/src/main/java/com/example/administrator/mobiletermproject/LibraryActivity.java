package com.example.administrator.mobiletermproject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
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
    private int[] resID = new int[FOLDER_NUMBER];//폴더 각각의 리스트 아이디를 저장할 배열
    private int[] textID = new int[FOLDER_NUMBER];//폴더 각각의 제목(EditText)을 저장할 배열
    private ListView[] folder_ListView = new ListView[FOLDER_NUMBER];//카드를 나열할 리스트뷰의 배열
    private Folder[] folders = new Folder[FOLDER_NUMBER];//폴더 객체 생성
    private TextView[] folderTitle= new TextView[FOLDER_NUMBER];//폴더의 제목을 표시할 EditText의 배열
    private ItemsListAdapter folder_Adapter;//리스트뷰에 쓰일 어댑터
    private ArrayList<String> nameOfFolder= new ArrayList<String>();//폴더명을 저장할 ArrayList
    private int folderIndex = -1;//이벤트가 발생한 곳이 어느 폴더인지 저장하는 변수 (0~3 값 범위)
    private String title="";//현재 Library의 이름을 저장

    private LinearLayout area1, area2, area3, area4;//각각 folder 0~3. 스크롤 이동 판정에 사용
    private int drgAndDrpCheck=0;//D&D의 시작 종료 판별
    private String beforePath;//D&D 시작한 파일의 경로
    private String afterPath;//D&D 종료한 파일의 경로
    int oldArea;//스크롤 이동 판정에 사용
    private HorizontalScrollView scrollview;//스크롤 이동할 가로 스크롤 뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);


        scrollview = ((HorizontalScrollView) findViewById(R.id.hr));
        scrollview.setOnDragListener(myScrollListener);//드래그시 스크롤 이동 리스너

        //각 폴더마다 area를 심어주고 D&D를 위해 리스너를 달아줌
        area1 = (LinearLayout)findViewById(R.id.folder1);
        area2 = (LinearLayout)findViewById(R.id.folder2);
        area3 = (LinearLayout)findViewById(R.id.folder3);
        area4 = (LinearLayout)findViewById(R.id.folder4);
        area1.setOnDragListener(myOnDragListener);
        area2.setOnDragListener(myOnDragListener);
        area3.setOnDragListener(myOnDragListener);
        area4.setOnDragListener(myOnDragListener);



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
            folderTitle[j]=(TextView) findViewById(folders[j].getTextId());
            folderTitle[j].setText(folders[j].getFolderName());

            // 폴더내의 카드들의 목록을 layout으로 출력하는 어댑터 생성
            folder_Adapter = new ItemsListAdapter(this, folders[j].nameOfCard);

            // activity_libraty.xml 의 리스트뷰 아이디를 읽어와서 연결
            folder_ListView[j] = (ListView) findViewById(folders[j].getlistVIewId());
            folder_ListView[j].setAdapter(folder_Adapter);
            //리스너
            folder_ListView[j].setOnItemClickListener(onClickListItem);

            //D&D 스크롤 이동 설정
            folder_ListView[j].setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            folder_ListView[j].setOnItemLongClickListener(myOnItemLongClickListener);//롱클릭 리스너

            //리스트뷰 속성
            folder_ListView[j].setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);//화면을 넘어가면 스크롤바 생성
            folder_ListView[j].setDivider((new ColorDrawable(Color.GRAY)));//구분선 회색
            folder_ListView[j].setDividerHeight(5); //구분선 굵기

        }
    }
    @Override
    protected void onResume() {
        //카드 리스트뷰 4개 초기화
        for(int j=0;j<FOLDER_NUMBER;j++) {
            resID[j] = getResources().getIdentifier("com.example.administrator.mobiletermproject:id/list" + j, null, null);
            textID[j] = getResources().getIdentifier("com.example.administrator.mobiletermproject:id/text" + j, null, null);
            folders[j]=new Folder(nameOfFolder.get(j), resID[j],textID[j], title);
            folderTitle[j]=(TextView) findViewById(folders[j].getTextId());
            folderTitle[j].setText(folders[j].getFolderName());

            // 폴더내의 카드들의 목록을 layout으로 출력하는 어댑터 생성
            folder_Adapter = new ItemsListAdapter(this, folders[j].nameOfCard);

            // activity_libraty.xml 의 리스트뷰 아이디를 읽어와서 연결
            folder_ListView[j] = (ListView) findViewById(folders[j].getlistVIewId());
            folder_ListView[j].setAdapter(folder_Adapter);
            //리스너
            folder_ListView[j].setOnItemClickListener(onClickListItem);

            //D&D 스크롤 이동 설정
            folder_ListView[j].setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            folder_ListView[j].setOnItemLongClickListener(myOnItemLongClickListener);//롱클릭 리스너
        }
        super.onResume();
    }


    private int getFolderIndex(){
        return folderIndex;
    }

    //드래그앤 드랍 정보 저장
    class PassObject{
        View view;
        String item;
        PassObject(View v, String i){
            view = v;
            item = i;
        }
    }

    //롱클릭 시 커서에 붙는 텍스트뷰
    static class ViewHolder {
        TextView text;
    }

    //카드의 드래그 앤 드롭을 위해 사용할 커스텀 리스트뷰 어댑터
    public class ItemsListAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> list;

        ItemsListAdapter(Context c, ArrayList<String> l){
            context = c;
            list = l;
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                rowView = inflater.inflate(R.layout.row, null);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.text = (TextView) rowView.findViewById(R.id.rowTextView);
                rowView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.text.setText(list.get(position).toString());

            return rowView;
        }
    }

    AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            String selectedItem = (String)(parent.getItemAtPosition(position));
            PassObject passObj = new PassObject(view, selectedItem);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, passObj, 0);
            // view.setBackgroundColor(getResources().getColor(android.R.color.black));

            return true;
        }

    };

    /*
        스크롤 이동 메소드
         */
    View.OnDragListener myScrollListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            float x = event.getX();
            if(x<200)
                scrollview.smoothScrollBy(-50,0);
            if(x>width-200)
                scrollview.smoothScrollBy(50,0);
            return true;
        }
    };

    /*
    드래그 앤 드롭 메소드
     */
    View.OnDragListener myOnDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int area;
            String passedItem;
            PassObject passObj;
            if(v == area1){
                area = 0;
            }else if(v == area2){
                area = 1;
            }else if(v == area3){
                area = 2;
            }else if(v == area4){
                area = 3;
            }else{
                area = -1;
            }

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    System.out.println("ACTION_DRAG_STARTED: " + area);
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    System.out.println("ACTION_DRAG_ENTERED: " + area);
                    passObj = (PassObject)event.getLocalState();
                    passedItem = passObj.item;
                    //폴더 내부에 커서가 있을 때 스크롤 이동
                    if(area>oldArea) {
                        scrollview.smoothScrollBy(50,0);
                        oldArea=area;
                    }
                    else if(area<oldArea){
                        scrollview.smoothScrollBy(-50,0);
                        oldArea=area;
                    }

                    if(drgAndDrpCheck==0) {
                        beforePath = "/data/data/com.example.administrator.mobiletermproject/files/" +
                                title + "/" + folders[area].getFolderName() + "/"  +passedItem+".txt";
                        System.out.println(">>before : "+beforePath);
                        drgAndDrpCheck=1;
                    }
                    oldArea=area;
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    System.out.println("ACTION_DRAG_EXITED: " + area);
                    break;

                case DragEvent.ACTION_DROP://드롭 한 순간
                    System.out.println("ACTION_DROP: " + area);
                    passObj = (PassObject)event.getLocalState();
                    passedItem = passObj.item;
                    if (drgAndDrpCheck == 1) {//파일 이동 시작
                        afterPath = "/data/data/com.example.administrator.mobiletermproject/files/" +
                                title + "/" + folders[area].getFolderName() + "/" + passedItem + ".txt";
                        //같은 이름의 카드가 존재 한다면
                        File file = new File(afterPath);
                        if (file.isFile()) {
                            if(beforePath.equals(afterPath)) {//드래그 시작한 폴더와 종료한 폴더가 같을 때
                            }
                            else {
                                Toast.makeText(getApplicationContext(),
                                        "이미 존재하는 카드입니다. 다른 곳으로 이동시켜주세요", Toast.LENGTH_SHORT).show();
                            }
                            drgAndDrpCheck = 0;
                            return false;
                        } else {//같은 이름의 카드가 없다면
                            moveFile(beforePath, afterPath);//파일 이동
                            drgAndDrpCheck = 0;
                            onResume();//뷰 다시 그림
                        }
                    }
                case DragEvent.ACTION_DRAG_ENDED:
                    System.out.println("ACTION_DRAG_ENDED: " + area);

                default:
                    break;
            }
            return true;
        }
    };

    /*
    파일을 이전 이후 디렉토리를 받아 이동하는 메소드
     */
    public String moveFile(String beforeFilePath, String afterFilePath) {
        try{
            File file =new File(beforeFilePath);
            if(file.renameTo(new File(afterFilePath))){ //파일 이동
                return afterFilePath; //성공시 성공 파일 경로 return
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //현재 클릭한 add버튼이 어느 카드리스트에 카드를 추가해야 할 것인지
    // folder 번호를 매기기 위해 사용하는 메서드 유가릿?
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

    /*
    카드 추가 버튼 이벤트 리스너
     */
    public RelativeLayout LayoutLoad() {
        //relativelayout 생성 id:0
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setId(0);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relativeParams.setMargins(20,20,20,20);
        relativeLayout.setLayoutParams(relativeParams);

        //텍스트뷰 1 id:1
        relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        TextView cardNameBox = new TextView(this);
        cardNameBox.setId(1);//에러아님
        cardNameBox.setPadding(40,40,40,20);
        cardNameBox.setTextSize(15);
        cardNameBox.setLayoutParams(relativeParams);
        cardNameBox.setText("Card Name");
        cardNameBox.setBackgroundColor(Color.parseColor("#2B78FF"));
        relativeLayout.addView(cardNameBox);

        //에딧텍스트 1 id:2
        relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardNameBox.getId());
        final EditText cardNameInput = new EditText(this);
        cardNameInput.setTextColor(Color.parseColor("#000000"));
        cardNameInput.setId(2);//에러아님
        cardNameInput.setPadding(40, 0, 40, 50);
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
        cardNameBox.setBackgroundColor(Color.parseColor("#2B78FF"));
        relativeLayout.addView(cardContentBox);

        //에딧텍스트 2 id:4
        relativeParams=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardContentBox.getId());
        final EditText cardContentInput = new EditText(this);
        cardContentInput.setTextColor(Color.parseColor("#000000"));
        cardContentInput.setId(4);//에러아님
        cardContentInput.setPadding(40,0,40,40);
        cardContentInput.setTextSize(20);
        cardContentInput.setLines(3);
        cardContentInput.setMaxLines(3);
        cardContentInput.setGravity(0);
        cardContentInput.setLayoutParams(relativeParams);
        relativeLayout.addView(cardContentInput);

        return relativeLayout;
    }

    public void onCardAddbtnClicked(View view){
        setFolderIndex(view); //현재 눌린 버튼이 어느 폴더에 속하는지 판별하는 folderIndex를 초기화

        //다이얼로그 생성
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //relativelayout 생성 id:0
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setId(0);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relativeParams.setMargins(20,20,20,20);
        relativeLayout.setLayoutParams(relativeParams);

        //텍스트뷰 1 id:1
        relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        TextView cardNameBox = new TextView(this);
        cardNameBox.setId(1);//에러아님
        cardNameBox.setPadding(40,40,40,20);
        cardNameBox.setTextSize(15);
        cardNameBox.setLayoutParams(relativeParams);
        cardNameBox.setText("Card Name");
        cardNameBox.setBackgroundColor(Color.parseColor("#8FABFF"));
        cardNameBox.setTextColor(Color.parseColor("#ffffff"));
        relativeLayout.addView(cardNameBox);

        //에딧텍스트 1 id:2
        relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        relativeParams.addRule(RelativeLayout.BELOW, cardNameBox.getId());
        final EditText cardNameInput = new EditText(this);
        cardNameInput.setTextColor(Color.parseColor("#000000"));
        cardNameInput.setId(2);//에러아님
        cardNameInput.setPadding(40, 0, 40, 50);
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
        cardContentBox.setBackgroundColor(Color.parseColor("#8FABFF"));
        cardContentBox.setTextColor(Color.parseColor("#ffffff"));
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

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        alert.setTitle("");
        alert.setView(relativeLayout);

        //Save 버튼을 눌럿을 때
        alert.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                folderIndex=getFolderIndex();
                String strTitle = cardNameInput.getText().toString(); //제목 저장
                String strContent = cardContentInput.getText().toString(); //내용 문자열 저장
                //만약 입력된 제목이 공백이거나 중복이라면
                if (folders[getFolderIndex()].nameOfCard.contains(strTitle) ){
                    Toast.makeText(getApplicationContext(),
                            "이름이 중복되었습니다.", Toast.LENGTH_SHORT).show();
                }

                else if ( strTitle.length() == 0 ) {
                    Toast.makeText(getApplicationContext(),
                            "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
                else { //정상 실행
                    folders[getFolderIndex()].nameOfCard.add(strTitle);//리스트뷰에 표시할 arraylist 갱신

                    String contentFilename = "/data/data/com.example.administrator.mobiletermproject/files/"
                            + title + "/" + folders[getFolderIndex()].getFolderName() + "/" + strTitle + ".txt";
                    // 제목파일 이름 너무 길어서 그냥 변수하나로 따로 뺌
                    //파일 생성
                    try {
                        FileOutputStream fos = new FileOutputStream(contentFilename, true);
                        //제목+.txt 형식으로 파일 생성.
                        fos.write(strContent.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                onResume();
                dialog.cancel();
            }
        });

        //Cancel 버튼을 눌럿을 때
        alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
            int folderChecker=-1;
            if(parent.getId()==R.id.list0)
                folderChecker=0;
            else if(parent.getId()==R.id.list1)
                folderChecker=1;
            else if(parent.getId()==R.id.list2)
                folderChecker=2;
            else if(parent.getId()==R.id.list3)
                folderChecker=3;
            else{}
            if(folderChecker==-1) {
                return ;
            }
            else {
                // 카드의 title과 경로 + 이름을 저장
                String titleData = folder_ListView[folderChecker].getItemAtPosition(position).toString(); // 어댑터의 0번쨰 정보

                // 카드의 디렉토리 주소
                // String cardFileFolder = "/" + folders[folderChecker].getFolderName() + "/" + titleData + ".txt";
                String cardFileFolder = folders[folderChecker].getFolderName();

                Intent intentLibtoCard = new Intent(getApplicationContext(), CardActivity.class); // 라이브러리 -> 카드 인텐트
                // title 정보와 content 파일 이름에 대한 정보를 인텐트로 받아 넘김

                // 카드 이름과 주소 전달
                intentLibtoCard.putExtra("Library_Title", title); // 라이브러리 데이터
                intentLibtoCard.putExtra("Card_FIle_Folder", cardFileFolder);
                intentLibtoCard.putExtra("Card_Title", titleData);
                // 전달할 것 : /title + /folders[folderChecker].getFolderName() + /titleData + .txt 로 전달하고픔
                // "/data/data/com.example.administrator.mobiletermproject/files/" 는 CardActivity에서 정의

                startActivity(intentLibtoCard);
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
}
