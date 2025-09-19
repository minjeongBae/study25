
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestPaser {

    final int ENTER_ASCII = 10;
    final int SPACE_ASCII = 32;
    final int COLON_ASCII = 58;


    private FileReader reader;

    private String method;
    private String uri;
    private String httpVersion;

    // 인덱스로 값 매핑
    private Map<String, String> headers;
    private ArrayList<FormDataTextFile> files;

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }


    /** parse() 수행 전 필수 수행 !!
    // 파일 Reader 생성 **/
    private void create(File file) throws FileNotFoundException {
        if(file.exists()){
            this.reader = new FileReader(file);
            System.out.println("** 파일 인식 완료 **");
            return;
        }
        System.out.println("** 파일 확인불가 **");
    }

    private void finishReading() throws IOException {
        if(this.reader != null){
            reader.close();
        }
    }


    /** http 헤더와 바디 (파일) 추출 **/
    public void parse(String fileURL) throws IOException {
        create(new File(fileURL));
        if(reader == null){
            System.out.println("파일 확인 불가");
            return;
        }
        findHeader();
        findFile();

        finishReading();
    }


    public void show() {
        // 추출한 거 확인용
        System.out.println(method + " " + uri + " " + httpVersion);
        System.out.println("---------------");
        for(Map.Entry<String, String> entry : headers.entrySet()){
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("\n-------**--------");
        for(FormDataTextFile file : files){
            System.out.println("---------------");
            System.out.println(file.getFileName());
            System.out.println(file.getContentType());
            System.out.println(file.getFormName());
            System.out.println(file.getBody());
            System.out.println("---------------");
        }
        System.out.println("-------**--------");

    }

    private String parseRequestLine(String buffer, int data) throws IOException {
        if(data == SPACE_ASCII) {
            // 띄어쓰기가 포함되어 있으면
            if(this.method == null || this.method.isEmpty()) {
                this.method = buffer.substring(0,buffer.length()-1);
            } else if (this.uri == null || this.uri.isEmpty()) {
                this.uri = buffer.substring(0, buffer.length()-1);
            }
            return "";
        } else if (data == ENTER_ASCII) {
            // 줄바꿈은 http 버전 직후
            this.httpVersion = buffer.substring(0,buffer.length()-1);
            return (char) reader.read() + "";
        }
        return buffer;
    }

    private void findHeader() throws IOException {
        String header = "";
        String value = "";
        headers = new HashMap<>();

        String buffer = "";
        int data = 0;
        int lineNum = 1;
        while((data=reader.read()) != -1) {
            if('\n'!= (char) data) buffer = buffer + (char) data;

            // Request line
            if(lineNum == 1) {
                buffer = parseRequestLine(buffer, data);
                if(httpVersion!=null && !httpVersion.trim().isEmpty()){
                    lineNum++;
                }
            } else {
                // 헤더 추출
                if(data == COLON_ASCII){
                    data = reader.read();
                    if(data == SPACE_ASCII) {
                        header=buffer.substring(0,buffer.length()-1);
                        buffer="";
                    } else {
                        buffer += (char) data;
                    }
                }
                // 헤더에 따른 데이터 추출
                if(data == ENTER_ASCII){
                    value=buffer.substring(0,buffer.length()-1);
                    buffer = "";
                    lineNum++;
                }
            }

            // 헤더 리스트에 넣기
            if(!header.isEmpty() && !value.isEmpty()){
                headers.put(header, value);
                header = "";
                value = "";
            }

            if(headers.containsKey("Content-Type")
                  && buffer.contains(getBoundary())){
                // 파일 시작
                return;
            }
        }
    }

    public String getBoundary(){
        String value = headers.get("Content-Type");
        String key = "boundary=";
        if(value==null){
            return "";
        }
        return value.substring(value.indexOf(key)+key.length(),value.length());
    }


    private void findFile() throws IOException{

        String HEADER_NAME1 = "Content-Disposition: ";
        String HEADER_NAME2 = "Content-Type: ";

        this.files = new ArrayList<>();
        FormDataTextFile file = new FormDataTextFile();

        String[] arr = new String[3];
        String cType = "";

        String boundary = getBoundary();
        int boundaryCnt = 1;

        int data = 0;
        String buffer = ""; // 버퍼
        while ((data = reader.read()) != -1) {
            buffer = buffer + (char) data;
            if(buffer.contains(boundary) && (boundaryCnt==0)){
                // 파일 시작
                buffer = "";
                boundaryCnt++;
            }

            if(buffer.contains(HEADER_NAME1) && (data == ENTER_ASCII)){
                String value = buffer.substring(HEADER_NAME1.length()+2,buffer.length()-1);

                arr[2] = value.substring(value.lastIndexOf("=")+1);
                value = value.substring(0,value.lastIndexOf("filename=")).trim();

                arr[1] = value.substring(value.lastIndexOf("=")+1,value.length()-1).trim();
                value = value.substring(0,value.lastIndexOf("name=")).trim();

                arr[0] = value.substring(0, value.length()-1).trim();

                buffer = "";
            }

            if(buffer.contains(HEADER_NAME2) && (data == ENTER_ASCII)){
                boundaryCnt = 1;
                cType = buffer.substring(HEADER_NAME2.length(), buffer.length()-1);
                buffer = "";
            }

            if(!cType.isEmpty()
                    && boundaryCnt==1
                    && buffer.contains(boundary)){
                boundaryCnt=0; // 파일 끝

                file.setContentType(cType);
                file.setFormName(arr[1]);
                file.setFileName(arr[2]);
                file.setContentType(arr[0]);
                file.setBody(buffer.substring(2, buffer.indexOf(boundary)-5));
                files.add(file);

                file = new FormDataTextFile();
                cType = "";
                arr = new String[3];

                buffer = "";
            }
        }
    }
}

