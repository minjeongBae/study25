package test;

import java.FormDataTextFile;
import java.RequestPaser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {
    //TODO 테스트 해야함
   // @Test
    public void test() throws IOException {
        RequestPaser paser = new RequestPaser();
        paser.create(new File("http_prac_data1.txt"));

        paser.findHeader();


        assertEquals(paser.getBoundary(), "Uee--r1_eDOWu7FpA0LJdLwCMLJQapQGu");



        ArrayList<FormDataTextFile> files = paser.findFile();

    }
}
