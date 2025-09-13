package java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestPaser {
    private FileReader reader;

    private String method;
    private String uri;
    private String httpVersion;

    // 인덱스로 값 매핑
    public Map<String, String> headers;

    public void create(File file) throws FileNotFoundException {
        if(file.exists()){
            this.reader = new FileReader(file);
            System.out.println("파일 인식");
            return;
        }
        System.out.println("** 파일 확인불가 **");
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void show() {
        for(Map.Entry<String, String> entry : headers.entrySet()){
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public void findHeader() throws IOException {
        if(reader == null){
            System.out.println("파일 확인 불가");
            return;
        }

        String header = "";
        String value = "";
        headers = new HashMap<>();

        String enterTarget = "[\\r][\\n]" ;
        String headerTarget = ": " ;
        String methodTarget = " /" ;
        String uriTarget = " HTTP" ;

        String now = "";
        int data = 0;
        int lineNum = 1;
        while((data=reader.read()) != -1) {
            if('\n'!= (char) data) now = now + (char) data;
            // Request line
            if(lineNum == 1) {
                // method 추출
                if((now.length() > methodTarget.length()) && now.substring(now.length()-methodTarget.length(),now.length()).equals(methodTarget)){
                    this.method = now.substring(0,now.length()-methodTarget.length());
                    now = "/";
                }

                // URI 추출
                if((now.length() > uriTarget.length()) && now.substring(now.length()-uriTarget.length(),now.length()).equals(uriTarget)) {
                    this.uri = now.substring(0, now.length() - uriTarget.length());
                    now = "HTTP";
                }

                // HTTP version 추출
                if(now.length() > enterTarget.length()
                        && now.substring(now.length()-enterTarget.length(),now.length()).equals(enterTarget)){
                    this.httpVersion = now.substring(0,now.length()-enterTarget.length());
                    now="";
                    data=reader.read();
                    lineNum++;
                }
            }

            if(now.trim().equals(enterTarget.trim())){
                return;
            }
            // 줄바꿈
            if(lineNum>1 && now.length() > enterTarget.length()
                    && now.substring(now.length()-enterTarget.length(),now.length()).equals(enterTarget)){
                value=now.substring(0,now.length()-enterTarget.length());
                now="";
                lineNum++;
            }

            // 헤더 추출
            if(now.length() > headerTarget.length()
                    && now.substring(now.length()-headerTarget.length(),now.length()).equals(headerTarget)){
                header=now.substring(0,now.length()-headerTarget.length()).trim();
                now="";
            }

            if(!header.equals("") && !value.equals("")){
                headers.put(header, value);
            }
        }

    }

    public String getBoundary(){
        String value = headers.get("Content-Type");
        String key = "boundary=";
        return value.substring(value.indexOf(key)+key.length(),value.length());
    }


    public ArrayList<FormDataTextFile> findFile() throws IOException{
        if (reader == null) {
            System.out.println("파일 확인 불가");
            return null;
        }

        String HEADER_NAME1 = "Content-Disposition: ";
        String HEADER_NAME2 = "Content-Type: ";
        String enterTarget = "[\\r][\\n]";

        ArrayList<FormDataTextFile> files = new ArrayList<>();
        FormDataTextFile file = new FormDataTextFile();

        String[] arr = new String[3];
        String cType = "";
        String body = "";

        String boundary = getBoundary();
        int boundaryCnt = 0;

        int data = 0;
        String now = "";
        while ((data = reader.read()) != -1) {
            if(now.contains(boundary+enterTarget)){  // 파일 시작
                now = "";
                boundaryCnt++;
            }
            now = now + (char) data;

            if(now.contains(HEADER_NAME1) && now.contains(enterTarget)){
                String value = now.substring(HEADER_NAME1.length()+2,now.length()-enterTarget.length());

                arr[2] = value.substring(value.lastIndexOf("=")+1);
                value = value.substring(0,value.lastIndexOf("filename=")).trim();

                arr[1] = value.substring(value.lastIndexOf("=")+1,value.length()-1).trim();
                value = value.substring(0,value.lastIndexOf("name=")).trim();

                arr[0] = value.substring(0, value.length()-1).trim();

                now = "";
            }
            if(now.contains(HEADER_NAME2) && now.contains(enterTarget)){
                cType = now.substring(HEADER_NAME2.length()+2, now.length() - enterTarget.length());
                now = "";
            }

            if(now.contains(enterTarget) && now.length() > enterTarget.length()*2
                    && !cType.equals("")){
                String tempNow = now.replaceFirst(enterTarget,"");
                if(tempNow.contains(enterTarget)){
                    body = tempNow.replace(enterTarget,"");

                    file.setContentType(cType);
                    file.setFormName(arr[1]);
                    file.setFileName(arr[2]);
                    file.setContentType(arr[0]);
                    file.setBody(body);
                    files.add(file);

                    file = new FormDataTextFile();
                    cType = "";
                    arr = new String[3];

                    now = "";
                }
            }
        }

        return files;
    }
}

