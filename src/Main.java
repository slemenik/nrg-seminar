import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import com.github.petvana.liblas.LasWriter;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Main {


    private static final String ARSO_LIDAR_URL = "http://gis.arso.gov.si/lidar/gkot/laz/b_35/D48GK/GK_470_97.laz";
    private static final String ARSO_ORTOPHOTO_URL = "http://gis.arso.gov.si/arcgis/rest/services/DOF_2016/MapServer/export";
    private static final int ORTO_PHOTO_IMG_SIZE = 1000;//TODO - change so it works with different sizes

    public static void main(String[] args){

        System.out.println("Started...");

        File file = createLidarFile(ARSO_LIDAR_URL);
        //File file = new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\src\\GK_470_97.laz");

        String[] fileNameParams = FilenameUtils.removeExtension(file.getName()).split("_"); //GK_470_97
        int xThousand = Integer.parseInt(fileNameParams[1]);
        int yThousand = Integer.parseInt(fileNameParams[2]);
        BufferedImage image = getOrtoPhoto(xThousand,yThousand);//470,97

//        BufferedImage image = null;
//        try {
//            image = ImageIO.read(new File("C:\\Users\\Matej\\IdeaProjects\\nrg-seminar\\src\\saved.png"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        DataBufferInt buff = (DataBufferInt) image.getRaster().getDataBuffer();
        int[] pixels = buff.getData();
        int height = image.getHeight();
        int width = image.getWidth();

        LASReader lasReader = null;
        try {
            lasReader = new LASReader(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Reading points...");
        for (LASPoint p: lasReader.getPoints()) {
            int[] pxCoordinates = findClosestPxCoordinates(p.getX(), p.getY(),  xThousand, yThousand);
            int i = pxCoordinates[0]-(xThousand*1000);
            int j = (height-1)-(pxCoordinates[1]-(yThousand*1000));//j index of image goes from top to bottom

            int rgb = pixels[(j*width)+i]; //binary int value
            Color color = new Color(rgb);
            int[] rgbArray = new int[]{color.getRed(), color.getGreen(), color.getBlue()};

            //System.out.println(rgbArray[0] + "," + rgbArray[1] + "," + rgbArray[2]);

        }

        System.out.println("Finished.");
        System.exit(0);

    }

    private static File createLidarFile(String URL){
        File f = null;
        InputStream inputStream;
        try {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target(URI.create(URL));
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
                .param("bbox", minX + ","+minY+","+maxX+","+maxY)
                .param("format", "bmp")
                .param("transparent", "false")
                .param("f", "image")
                .param("size", ORTO_PHOTO_IMG_SIZE + "," + ORTO_PHOTO_IMG_SIZE);

        System.out.print("Requesting from source: [" + ARSO_ORTOPHOTO_URL + "]");
        Response response = client.target(ARSO_ORTOPHOTO_URL).request().post(Entity.form(form));
        BufferedImage image = response.readEntity(BufferedImage.class);
        response.close();
        System.out.println("[DONE]");

        return image;
    }

    private static int[] findClosestPxCoordinates(int x, int y, int minX, int minY){//TODO - find if/why do we need ints? LASreader??

        double _x = (double)x/100;//we get x without .00 decimal. why?
        double _y = (double)y/100;
        return findClosestPxCoordinates(_x, _y,  minX,  minY);
    }

    private static int[] findClosestPxCoordinates(double x, double y, int minX, int minY){

        double maxX = minX + (ORTO_PHOTO_IMG_SIZE-1);
        double maxY = minY + (ORTO_PHOTO_IMG_SIZE-1);

        Point2D p = new Point2D.Double(x,y);
        Point2D upperLeft = new Point2D.Double((int)x,(int)y+1);
        Point2D upperRight = new Point2D.Double((int)x+1,(int)y+1);
        Point2D bottomLeft = new Point2D.Double((int)x,(int)y);
        Point2D bottomRight = new Point2D.Double((int)x+1,(int)y);

        PriorityQueue<Point2D> queue = new PriorityQueue<Point2D>(new Comparator() {
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

        //if pixel is allowed, add to queue
        queue.add(bottomLeft);
        if ((int)y+1 <= maxY) queue.add(upperLeft);
        if ((int)x+1 <= maxX) queue.add(bottomRight);
        if ((int)x+1 <= maxX && (int)y+1 <= maxY) queue.add(upperRight);
        Point2D closestPoint = queue.peek();

        return new int[]{(int)closestPoint.getX(), (int)closestPoint.getY()};
    }
}
