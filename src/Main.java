import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import com.github.mreutegg.laszip4j.laszip.LASpoint;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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


    public static final String ARSO_LIDAR_URL = "http://gis.arso.gov.si/lidar/gkot/laz/b_35/D48GK/GK_470_97.laz";

    public static void main(String[] args){

        System.out.println("Started...");

        //download LIDAR file
        File file = createLidarFile(ARSO_LIDAR_URL);


        //calculate colors
        //calculate normals
        //write to file

        LASReader lasReader = null;
        try {
            lasReader = new LASReader(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = 0;
        System.out.println("Reading points");
        for (LASPoint p: lasReader.getPoints()) {
            byte b = p.getClassification();

            if (i++>20) break;
            System.out.println(p.getX());
        }


    }

    public static File createLidarFile(String URL){
        File f = null;
        InputStream inputStream = null;
        try {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target(URI.create(ARSO_LIDAR_URL));
            Invocation.Builder request = resource.request();
            request.accept(MediaType.APPLICATION_OCTET_STREAM);

            System.out.println("Requesting from source [" + resource.getUri() + "]");
            Response response = request.get();

            System.out.print("Reading response...");
            inputStream = response.readEntity(InputStream.class);

            String fileName = FilenameUtils.getName(resource.getUri().getPath());
            f = new File(fileName);
            f.deleteOnExit();

            System.out.print("[DONE]\nWriting to file...");
            FileUtils.copyInputStreamToFile(inputStream, f);
            inputStream.close();

            System.out.println("[DONE]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }
}
