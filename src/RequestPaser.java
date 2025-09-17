
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestPaser {

    final String enterTarget = "[\\r][\\n]" ;


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

    // parse() 수행 전 필수 수행 !!
    // 파일 Reader 생성
    private void create(File file) throws FileNotFoundException {
        if(file.exists()){
            this.reader = new FileReader(file);
            System.out.println("** 파일 인식 완료 **");
            return;
        }
        System.out.println("** 파일 확인불가 **");
    }

    // http 헤더와 바디 (파일) 추출
    public void parse(String fileURL) throws IOException {
        create(new File(fileURL));
        if(reader == null){
            System.out.println("파일 확인 불가");
            return;
        }
        findHeader();
        findFile();
    }


    public void show() {

        System.out.println(method + " " + uri + " " + httpVersion);

        for(Map.Entry<String, String> entry : headers.entrySet()){
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private String parseRequestLine(String buffer){
        // Request line은 띄어쓰기로 구분
        String divTarget = " " ;
        if(buffer.contains(divTarget)) {
            // 띄어쓰기가 포함되어 있으면
            if(this.method == null || this.method.isEmpty()) {
                this.method = buffer.substring(0,buffer.length()-divTarget.length());
                return "";
            } else if (this.uri == null || this.uri.isEmpty()) {
                this.uri = buffer.substring(0, buffer.length() - divTarget.length());
                return "";
            }
        } else if (buffer.contains(enterTarget)) {
            this.httpVersion = buffer.substring(0,buffer.length()-enterTarget.length());
            return "";
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
                buffer = parseRequestLine(buffer);
                if(httpVersion!=null && !httpVersion.trim().isEmpty()){
                    data=reader.read();
                    lineNum++;
                }
            }

            if(buffer.trim().equals(enterTarget.trim())){
                return;
            }
            // 줄바꿈
            if(lineNum>1 && buffer.length() > enterTarget.length()
                    && buffer.substring(buffer.length()-enterTarget.length(),buffer.length()).equals(enterTarget)){
                value=buffer.substring(0,buffer.length()-enterTarget.length());
                buffer="";
                lineNum++;
            }

            String headerTarget = ": " ;
            // 헤더 추출
            if(buffer.length() > headerTarget.length()
                    && buffer.substring(buffer.length()-headerTarget.length(),buffer.length()).equals(headerTarget)){
                header=buffer.substring(0,buffer.length()-headerTarget.length()).trim();
                buffer="";
            }

            if(!header.isEmpty() && !value.isEmpty()){
                headers.put(header, value);
            }
        }

    }

    public String getBoundary(){
        String value = headers.get("Content-Type");
        String key = "boundary=";
        return value.substring(value.indexOf(key)+key.length(),value.length());
    }


    private void findFile() throws IOException{
        String HEADER_NAME1 = "Content-Disposition: ";
        String HEADER_NAME2 = "Content-Type: ";

        this.files = new ArrayList<>();
        FormDataTextFile file = new FormDataTextFile();

        String[] arr = new String[3];
        String cType = "";
        String body = "";

        String boundary = getBoundary();
        int boundaryCnt = 0;

        int data = 0;
        String buffer = ""; // 버퍼
        while ((data = reader.read()) != -1) {
            if(buffer.contains(boundary+enterTarget)){  // 파일 시작
                buffer = "";
                boundaryCnt++;
            }
            buffer = buffer + (char) data;

            if(buffer.contains(HEADER_NAME1) && buffer.contains(enterTarget)){
                String value = buffer.substring(HEADER_NAME1.length()+2,buffer.length()-enterTarget.length());

                arr[2] = value.substring(value.lastIndexOf("=")+1);
                value = value.substring(0,value.lastIndexOf("filename=")).trim();

                arr[1] = value.substring(value.lastIndexOf("=")+1,value.length()-1).trim();
                value = value.substring(0,value.lastIndexOf("name=")).trim();

                arr[0] = value.substring(0, value.length()-1).trim();

                buffer = "";
            }
            if(buffer.contains(HEADER_NAME2) && buffer.contains(enterTarget)){
                cType = buffer.substring(HEADER_NAME2.length()+2, buffer.length() - enterTarget.length());
                buffer = "";
            }

            if(buffer.contains(enterTarget) && buffer.length() > enterTarget.length()*2
                    && !cType.equals("")){
                String tempNow = buffer.replaceFirst(enterTarget,"");
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

                    buffer = "";
                }
            }
        }
    }
}

