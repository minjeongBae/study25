import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        RequestPaser paser = new RequestPaser();
        paser.create(new File("http_prac_data1.txt"));

        paser.makeMap();
        System.out.println("-----------------------------------");
        paser.show(); // todo 뭔가 이상함
    }
}