package com.example.administrator.mobiletermproject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * 라이브러리 내의 폴더들의 정보를 저장하고, 폴더내의 카드들의 목록을 가짐
 */

public class Folder extends LibraryActivity{
    private String folderName;
    private int listVIewID,textID;
    public ArrayList<String> nameOfCard= new ArrayList<String>();//카드들 이름을 저장할 ArrayList
    private String libTitle="";

    //생성자
    public Folder(String name, int resID, int textID, String title){
        setFolderName(name); //폴더이름 초기화
        setTextId(textID);//TextId 초기화
        setlistVIewId(resID);//listVIewID 초기화
        setLibTitle(title);//libTitle 초기화

        //현재 폴더내의 카드들(텍스트파일)의 이름을 nameOfCard 리스트뷰에 업데이트
        updateNOCList();
    }

    public void setLibTitle(String title){
        libTitle=title;
        return ;
    }
    public String getLibTitle(){
        return libTitle;
    }

    public void setlistVIewId(int id){
        listVIewID=id;
        return ;
    }
    public int getlistVIewId(){
        return listVIewID;
    }

    public void setTextId(int id){
        textID=id;
        return ;
    }
    public int getTextId(){
        return textID;
    }

    public void setFolderName(String name){
        folderName=name;
        return ;
    }

    public String getFolderName(){
        return folderName;
    }

    //폴더내 카드들의 목록을 nameOfCard에 업데이트
    public void updateNOCList(){
        try{
            FilenameFilter fileFilter = new FilenameFilter(){//확장자 필터
                public boolean accept(File dir, String name){
                    return name.endsWith("txt"); //txt만 취급
                }
            };
            File file = new File("/data/data/com.example.administrator.mobiletermproject/files/"+getLibTitle()+"/"+getFolderName()); //폴더 이름의 경로로 접근
            File[] files = file.listFiles(fileFilter);//확장자 필터 적용

            //콘솔창에 현재 폴더에 있는 파일 출력 및 어레이리스트 업데이트
            System.out.println("        <<  폴더 : "+getFolderName()+"   >>    ");
            System.out.println("다음과 같은 카드들이 있습니다.");
            if(files.length==0)System.out.println("Nothing to show");
            nameOfCard.clear();//arraylist 초기화
            for (int i = 0; i < files.length; i++) {//파일 갯수 만큼 돌면서 arraylist에 값 삽입
                nameOfCard.add(files[i].getName().substring(0,files[i].getName().length()-4));
                System.out.println(i + " : " + files[i].getName());
            }
            return ;
        } catch( Exception e ){
            return ;
        }
    }
}
