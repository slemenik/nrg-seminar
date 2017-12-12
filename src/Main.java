import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import com.github.mreutegg.laszip4j.laszip.LASpoint;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.internal.HttpUrlConnector;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(URI.create("http://gis.arso.gov.si/lidar/gkot/laz/b_35/D48GK/GK_470_97.laz"));
        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_OCTET_STREAM);

        Response response = request.get();


        System.out.println(response.getEntity().getClass());

        InputStream inputStream = response.readEntity(InputStream.class);

        File f = new File("temp.laz");
        f.deleteOnExit();


        try {
            FileUtils.copyInputStreamToFile(inputStream, f);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //inputStream.close();

        //File f = new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\GK_521_144.laz");
        LASReader lasReader = null;
        try {
            lasReader = new LASReader(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = 0;
        for (LASPoint p: lasReader.getPoints()) {
            byte b = p.getClassification();

            if (i++>20) break;
            System.out.println(p.getX());
        }


    }
}
