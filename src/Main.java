import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        RequestPaser paser = new RequestPaser();
        paser.create(new File("http_prac_data1.txt"));

        paser.findHeader();
        paser.show();

        System.out.println("-----------------------------------");

        ArrayList<FormDataTextFile> files = paser.findFile();


    }
}