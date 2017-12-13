import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


import javax.imageio.ImageIO;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Main {


    private static final String ARSO_LIDAR_URL = "http://gis.arso.gov.si/lidar/gkot/laz/b_35/D48GK/GK_470_97.laz";
    private static final String ARSO_ORTOPHOTO_URL = "http://gis.arso.gov.si/arcgis/rest/services/DOF_2016/MapServer/export";
    private static final int ORTO_PHOTO_IMG_SIZE = 1000;//TODO - change so it works with different sizes

    public static void main(String[] args){

        System.out.println("Started...");

        //download LIDAR file
        //File file = createLidarFile(ARSO_LIDAR_URL);
        File file = new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\src\\GK_470_97.laz");
        //calculate colors

        String[] fileNameParams = FilenameUtils.removeExtension(file.getName()).split("_"); //GK_470_97
//        BufferedImage image = getOrtoPhoto(Integer.parseInt(fileNameParams[1]),Integer.parseInt(fileNameParams[2]));//470,97
//        //BufferedImage image = null;
//        try {
//            //image = ImageIO.read(new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\src\\saved.png"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        DataBufferInt buff = (DataBufferInt) image.getRaster().getDataBuffer();
//        int[] pixels = buff.getData();
//        System.out.println(image.getWidth());
//        System.out.println(image.getHeight());
//        System.out.println(pixels.length);
        //System.out.println(image.getAlphaRaster());

        //System.out.println((double)7012332/100);

        LASReader lasReader = null;
        try {
            lasReader = new LASReader(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = 0;
        System.out.println("Reading points...");
        for (LASPoint p: lasReader.getPoints()) {
            //byte b = p.getClassification();
            i++;
            if(i>3) {
                findClosestPx(p.getX(), p.getY());
                //System.out.print(p.getX() + ",");
                //System.out.print(p.getY() + ",");
                break;
            }
            if (i%1000000==0) System.out.println(i);


        }

//        System.out.println(i);
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
            //response.close();

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

    private static BufferedImage getOrtoPhoto(int leftX, int lowerY){

        double minX = leftX * 1000;
        double minY = lowerY * 1000;
        double maxX = minX + (999.999999999);
        double maxY = minY + (999.999999999);

        Client client = ClientBuilder.newClient();
        Form form = new Form()
                .param("bbox", "361771.9704999999,73918.74945126937,638968.9895000001,209948.85829478354")
                .param("format", "bmp")
                .param("transparent", "false")
                .param("f", "image")
                .param("mapScale", "1000000");
//        Form form = new Form()
//                .param("bbox", minX + ","+minY+","+maxX+","+maxY)
//                .param("format", "bmp")
//                .param("transparent", "false")
//                .param("f", "image")
//                .param("size", ORTO_PHOTO_IMG_SIZE + "," + ORTO_PHOTO_IMG_SIZE);

        System.out.print("Requesting from source: [" + ARSO_ORTOPHOTO_URL + "]");
        Response response = client.target(ARSO_ORTOPHOTO_URL).request().post(Entity.form(form));
        BufferedImage image = response.readEntity(BufferedImage.class);
        response.close();
        System.out.println("[DONE]");


        return image;
    }

    private static void findClosestPx(int x, int y){//TODO - find why do we need ints? LASreader??

        double _x = (double)x/100;
        double _y = (double)y/100;
        findClosestPx(_x, _y);



    }

    private static void findClosestPx(double x, double y){

        //TODO check if always 4 pixels
        double leftX = ((int)x/1000)*1000;
        double rightX = leftX + 1000;
        double bottomY = ((int)y/1000)*1000;
        double upperY = bottomY + 1000;

        Point2D p = new Point2D.Double(x,y);
        Point2D upperLeft = new Point2D.Double(leftX,upperY);
        Point2D upperRight = new Point2D.Double(rightX,upperY);
        Point2D bottomLeft = new Point2D.Double(leftX,bottomY);
        Point2D bottomRight = new Point2D.Double(rightX,bottomY);

        PriorityQueue<Point2D> queue = new PriorityQueue(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Point2D p1 = (Point2D) o1;
                Point2D p2 = (Point2D) o2;

                if (p.distance(p1) < p.distance(p2)){
                    return -1;
                } else { //no zero needed
                    return 1;
                }
            }
        });

        queue.add(upperLeft);
        queue.add(upperRight);
        queue.add(bottomLeft);
        queue.add(bottomRight);
        Point2D closestPoint = queue.peek();



    }
}
