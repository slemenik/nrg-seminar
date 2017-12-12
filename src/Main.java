import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import com.github.mreutegg.laszip4j.laszip.LASpoint;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.client.internal.HttpUrlConnector;


import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;



public class Main {


    private static final String ARSO_LIDAR_URL = "http://gis.arso.gov.si/lidar/gkot/laz/b_35/D48GK/GK_470_97.laz";
    private static final String ARSO_ORTOPHOTO_URL = "http://gis.arso.gov.si/arcgis/rest/services/DOF_2016/MapServer/export";

    public static void main(String[] args){

        System.out.println("Started...");

        //download LIDAR file
//        File file = createLidarFile(ARSO_LIDAR_URL);
        File file = new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\GK_470_97.laz");
        //calculate colors

        String[] fileNameParams = FilenameUtils.removeExtension(file.getName()).split("_"); //GK_470_97
        BufferedImage image = getOrtoPhoto(Integer.parseInt(fileNameParams[1]),Integer.parseInt(fileNameParams[2]));//470,97

//        LASReader lasReader = null;
//        try {
//            lasReader = new LASReader(file);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        int i = 0;
//        System.out.println("Reading points...");
//        for (LASPoint p: lasReader.getPoints()) {
//            byte b = p.getClassification();
//
//            if (i++>20) break;
//            System.out.println(p.getX());
//        }


        //calculate normals
        //write to file
        System.exit(0);

    }

    public static File createLidarFile(String URL){
        File f = null;
        InputStream inputStream;
        try {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target(URI.create(ARSO_LIDAR_URL));
            Invocation.Builder request = resource.request();
            request.accept(MediaType.APPLICATION_OCTET_STREAM);

            System.out.println("Requesting from source [" + resource.getUri() + "]");
            Response response = request.get();

            System.out.print("Reading response...");
            inputStream = response.readEntity(InputStream.class);
            response.close();

            String fileName = FilenameUtils.getBaseName(resource.getUri().getPath());
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

    private static BufferedImage getOrtoPhoto(int leftX, int lowerY){

        double minX = leftX * 1000;
        double minY = lowerY * 1000;
        double maxX = minX + (999.999999999);
        double maxY = minY + (999.999999999);

        Client client = ClientBuilder.newClient();
        Form form = new Form()
                .param("bbox", minX + ","+minY+","+maxX+","+maxY)
                .param("format", "bmp")
                .param("transparent", "false")
                .param("f", "image")
                .param("size", "1000,1000");

        System.out.print("Requesting from source: [" + ARSO_ORTOPHOTO_URL + "]");
        Response response = client.target(ARSO_ORTOPHOTO_URL).request().post(Entity.form(form));
        BufferedImage image = response.readEntity(BufferedImage.class);
        response.close();
        System.out.println("[DONE]");


        return image;
    }


}
