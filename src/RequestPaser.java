import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPaser {
    private FileReader reader;

    private String method;
    private String uri;
    private String httpVerison;

    // 인덱스로 값 매핑
    private Map<String, String> headers;

    public void create(File file) throws FileNotFoundException {
        if(file.exists()){
            this.reader = new FileReader(file);
            System.out.println("파일 인식");
            return;
        }
        System.out.println("** 파일 확인불가 **");
    }

    public void makeMap() throws IOException {
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
                    this.httpVerison = now.substring(0,now.length()-enterTarget.length());
                    now="";
                    data=reader.read();
                    lineNum++;
                }
            }

            if(now.trim().equals(enterTarget.trim())){
                System.out.println("헤더 추출 끝");
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
                header=now.substring(0,now.length()-headerTarget.length());
                now="";
            }

            if(!header.equals("") && !value.equals("")){
                headers.put(header, value);
            }
        }

    }

    public void show() {
        for(Map.Entry<String, String> entry : headers.entrySet()){
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public String getValue(String key) {
        return headers.get(key);
    }

    public void makeFile(){

    }
}
