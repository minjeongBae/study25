import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        RequestPaser paser = new RequestPaser();
        paser.parse("http_prac_data1.txt");

        paser.show();
    }
}