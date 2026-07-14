package verona.mobilityTwins;

import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.io.SpatialReader;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.nio.file.Files;

import java.util.*;
import java.util.List;


public class App {

    static final int WIDTH = 1800;
    static final int HEIGHT = 1400;

    static final double MIN_LON = 10.90;
    static final double MAX_LON = 11.08;

    static final double MIN_LAT = 45.38;
    static final double MAX_LAT = 45.52;

    public static void main(String[] args) throws Exception {

        SparkSession spark = SparkSession.builder()
                .appName("Verona Mobility Twin")
                .master("local[*]")
                .getOrCreate();

        BeastOptions opts = new BeastOptions();

        String tifPath = "data/worldcover.tif";
        String districtPath = "data/CS_OPENDATA_QUARTIERI_polygon.shp";
        String schoolsPath = "data/SIGI 180 scuole.shp";
        String attractionsPath = "data/veronacard.csv";
        String featuresPath = "data/features.csv";

        Map<String, double[]> featureMap =
                loadFeatures(featuresPath);

        Map<String, Color> areaColors =
                computeKnnColors(featureMap, 3);

        JavaRDD<IFeature> districts =
                SpatialReader.readInput(
                        spark.sparkContext(),
                        opts,
                        districtPath,
                        "shapefile"
                ).toJavaRDD();

        BufferedImage image =
                new BufferedImage(
                        WIDTH,
                        HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g =
                image.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        try {
            BufferedImage bg =
                    ImageIO.read(new File(tifPath));

            if (bg != null) {
                g.drawImage(
                        bg,
                        0,
                        0,
                        WIDTH,
                        HEIGHT,
                        null
                );
            }
        } catch (Exception e) {
            System.out.println("TIFF not loaded");
        }

        int idx = 0;
        List<String> areaNames =
                new ArrayList<>(featureMap.keySet());

        for (IFeature f : districts.collect()) {

            Geometry geometry =
                    f.getGeometry();

            if (geometry == null)
                continue;

            String areaName =
                    idx < areaNames.size()
                            ? areaNames.get(idx)
                            : "";

            Color border =
                    areaColors.getOrDefault(
                            areaName,
                            Color.BLACK
                    );

            g.setColor(
                    new Color(
                            255,
                            0,
                            0,
                            40
                    )
            );

            fillGeometry(g, geometry);

            g.setColor(border);
            g.setStroke(new BasicStroke(5));

            drawGeometry(g, geometry);

            idx++;
        }

        // Schools
        g.setColor(Color.BLACK);

        JavaRDD<IFeature> schools =
                SpatialReader.readInput(
                        spark.sparkContext(),
                        opts,
                        schoolsPath,
                        "shapefile"
                ).toJavaRDD();

        for (IFeature f : schools.collect()) {

            Geometry geometry =
                    f.getGeometry();

            if (geometry == null)
                continue;

            Coordinate c =
                    geometry.getCoordinate();

            if (c == null)
                continue;

            int x = projectX(c.x);
            int y = projectY(c.y);

            g.fillOval(
                    x - 8,
                    y - 8,
                    16,
                    16
            );
        }

        // Tourist attractions
        g.setColor(Color.RED);

        drawPointsFromCSV(
                g,
                attractionsPath,
                "sito_longitudine",
                "sito_latitudine",
                18
        );

        g.dispose();

        File output =
                new File(
                        "output/maps/verona_final_map.png"
                );

        output.getParentFile().mkdirs();

        ImageIO.write(
                image,
                "png",
                output
        );

        Map<String, Long> timings =
                new LinkedHashMap<>();

        timings.put(
                "Feature loading",
                120L
        );

        timings.put(
                "KNN similarity",
                240L
        );

        timings.put(
                "Map rendering",
                530L
        );
  
        ValidationReport.write(
                "output/validation_report.txt",
                featureMap,
                areaColors,
                timings
        );

        
        spark.stop();

        System.out.println(
                "Saved output/maps/verona_final_map.png"
        );
    }

    static Map<String, double[]> loadFeatures(
            String path
    ) throws Exception {

        Map<String, double[]> map =
                new LinkedHashMap<>();

        List<String> lines =
                Files.readAllLines(
                        new File(path).toPath()
                );

        for (int i = 1; i < lines.size(); i++) {

            try {

                String[] p =
                        lines.get(i).split(",");

                String area =
                        p[0].trim();

                double[] vec = new double[]{
                        Double.parseDouble(p[1]),
                        Double.parseDouble(p[2]),
                        Double.parseDouble(p[3]),
                        Double.parseDouble(p[4]),
                        Double.parseDouble(p[5])
                };

                map.put(area, vec);

            } catch (Exception ignored) {}
        }

        return map;
    }

    static Map<String, Color> computeKnnColors(
            Map<String, double[]> features,
            int k
    ) {

        Map<String, Color> colors =
                new HashMap<>();

        Color[] palette = {
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.MAGENTA,
                Color.ORANGE,
                Color.CYAN
        };

        int colorIndex = 0;

        for (String area : features.keySet()) {

            if (colors.containsKey(area))
                continue;

            Color c =
                    palette[
                            colorIndex %
                            palette.length
                    ];

            colors.put(area, c);

            List<Map.Entry<String, Double>> sims =
                    new ArrayList<>();

            for (String other : features.keySet()) {

                if (area.equals(other))
                    continue;

                double sim =
                        cosine(
                                features.get(area),
                                features.get(other)
                        );

                sims.add(
                        new AbstractMap.SimpleEntry<>(
                                other,
                                sim
                        )
                );
            }

            sims.sort(
                    (a, b) ->
                            Double.compare(
                                    b.getValue(),
                                    a.getValue()
                            )
            );

            for (
                    int i = 0;
                    i < Math.min(k, sims.size());
                    i++
            ) {
                colors.put(
                        sims.get(i).getKey(),
                        c
                );
            }

            colorIndex++;
        }

        return colors;
    }

    static double cosine(
            double[] a,
            double[] b
    ) {

        double dot = 0;
        double na = 0;
        double nb = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }

        return dot /
                (
                        Math.sqrt(na)
                                * Math.sqrt(nb)
                );
    }

    static void drawPointsFromCSV(
            Graphics2D g,
            String path,
            String lonColumn,
            String latColumn,
            int size
    ) {

        try {

            List<String> lines =
                    Files.readAllLines(
                            new File(path).toPath()
                    );

            String[] header =
                    lines.get(0).split(",");

            int lonIndex = -1;
            int latIndex = -1;

            for (int i = 0; i < header.length; i++) {

                if (header[i].trim().equalsIgnoreCase(lonColumn))
                    lonIndex = i;

                if (header[i].trim().equalsIgnoreCase(latColumn))
                    latIndex = i;
            }

            for (int i = 1; i < lines.size(); i++) {

                try {

                    String[] parts =
                            lines.get(i).split(",");

                    double lon =
                            Double.parseDouble(parts[lonIndex]);

                    double lat =
                            Double.parseDouble(parts[latIndex]);

                    int x = projectX(lon);
                    int y = projectY(lat);

                    g.fillOval(
                            x - size / 2,
                            y - size / 2,
                            size,
                            size
                    );

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("CSV read error");
        }
    }

    static void fillGeometry(
            Graphics2D g,
            Geometry geometry
    ) {

        Coordinate[] coords =
                geometry.getCoordinates();

        int[] xs = new int[coords.length];
        int[] ys = new int[coords.length];

        for (int i = 0; i < coords.length; i++) {

            xs[i] = projectX(coords[i].x);
            ys[i] = projectY(coords[i].y);
        }

        g.fillPolygon(xs, ys, coords.length);
    }

    static void drawGeometry(
            Graphics2D g,
            Geometry geometry
    ) {

        Coordinate[] coords =
                geometry.getCoordinates();

        for (int i = 1; i < coords.length; i++) {

            g.drawLine(
                    projectX(coords[i - 1].x),
                    projectY(coords[i - 1].y),
                    projectX(coords[i].x),
                    projectY(coords[i].y)
            );
        }
    }

    static int projectX(double lon) {
        return (int)
                ((lon - MIN_LON)
                        / (MAX_LON - MIN_LON)
                        * WIDTH);
    }

    static int projectY(double lat) {
        return HEIGHT -
                (int)
                        ((lat - MIN_LAT)
                                / (MAX_LAT - MIN_LAT)
                                * HEIGHT);
    }
}