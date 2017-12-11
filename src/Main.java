import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import com.github.mreutegg.laszip4j.laszip.LASpoint;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;


public class Main {

    public static void main(String[] args){

        //get file
        //read file
        //calculate colors
        //calculate normals
        //write to file
        //http://gis.arso.gov.si/lidar/gkot/b_23/D48GK/GK_545_141.zlas


//        URL url = null;
//        try {
//            url = new URL("http://gis.arso.gov.si/lidar/gkot/b_23/D48GK/GK_545_141.zlas");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        File f = new File("temp");
//        //System.out.print(f.getName());
//
//        try {
//            FileUtils.copyURLToFile(url, f);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        File f = new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\GK_521_144.laz");
        LASReader lasReader = null;
        try {
            lasReader = new LASReader(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = 0;
        for (LASPoint p:
             lasReader.getPoints()) {
            byte b = p.getClassification();
            if (i++>20) break;
            System.out.println(b);
        }
        System.out.print(1);


    }
}
