package com.example.administrator.mobiletermproject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wooyo on 2016-10-28.
 */

// 메모에 대한 파일을 저장하는 곳
public class FileControl {
    Context mContext = null;

    public FileControl (Context _context) {
        mContext = _context;
    }

    // 파일에 문자열 데이터를 쓰는 메소드
    public boolean save(String data, String FILE_PATH) {

        if (data == null || data.isEmpty() == true) {
            return false;
        }
        else {
            try {
                FileOutputStream fos = new FileOutputStream(FILE_PATH, true);
                // 제목+.txt 형식으로 파일 생성.
               fos.write(data.getBytes());

                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    // 파일에서 데이터를 읽음
    public String load(String FILE_NAME) {
        try {
            FileInputStream fis = new FileInputStream(FILE_NAME);
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));

            String content="",temp="";
            while( (temp= bufferReader.readLine()) != null )
                content += temp;
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void delete(String FILE_PATH) {
        File file = new File(FILE_PATH);
        file.delete();    //root 삭제
    }
}