package verona.mobilityTwins;

import java.awt.Color;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class ValidationReport {

    public static void write(
            String outputPath,
            Map<String, double[]> features,
            Map<String, Color> knnGroups,
            Map<String, Long> timings
    ) throws Exception {

        double avgSimilarity =
                computeAverageSimilarity(features);

        try (
                PrintWriter pw =
                        new PrintWriter(
                                new FileWriter(outputPath)
                        )
        ) {

            pw.println("# Verona Mobility Twin - Validation Report");
            pw.println();

            pw.println("## Dataset Summary");
            pw.println(
                    "Neighborhoods: " + features.size()
            );

            pw.println(
                    "Features: vehicle_index, bike_index, pedestrian_index, tourist_visits, roads"
            );

            pw.println();

            pw.println("## Similarity Evaluation");
            pw.printf(
                    "Average cosine similarity: %.4f%n",
                    avgSimilarity
            );

            pw.println(
                    "Top-K neighbors: 3"
            );

            pw.println();

            pw.println("## Runtime");

            for (
                    Map.Entry<String, Long> entry :
                    timings.entrySet()
            ) {

                pw.println(
                        entry.getKey()
                                + ": "
                                + entry.getValue()
                                + " ms"
                );
            }

            pw.println();

            pw.println("## Example Similar Neighborhoods");

            List<String> names =
                    new ArrayList<>(
                            features.keySet()
                    );

            for (
                    int i = 0;
                    i < Math.min(5, names.size());
                    i++
            ) {

                String area =
                        names.get(i);

                List<String> neighbors =
                        findNearestNeighbors(
                                area,
                                features,
                                3
                        );

                pw.println(
                        "- " +
                        area +
                        " -> " +
                        String.join(", ", neighbors)
                );
            }

            pw.println();
            pw.println("## Interpretation");

            pw.println(
                "Districts that share the same border color on the final map are districts with similar mobility and urban feature profiles according to K-nearest neighbors similarity analysis. The listed neighbors above provide concrete examples of these relationships."
            );
            

        }
    }

    private static List<String> findNearestNeighbors(
            String queryArea,
            Map<String, double[]> features,
            int k
    ) {

        List<Map.Entry<String, Double>> sims =
                new ArrayList<>();

        double[] query =
                features.get(queryArea);

        for (String other :
                features.keySet()) {

            if (other.equals(queryArea))
                continue;

            double sim =
                    cosine(
                            query,
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

        List<String> result =
                new ArrayList<>();

        for (
                int i = 0;
                i < Math.min(k, sims.size());
                i++
        ) {
            result.add(
                    sims.get(i).getKey()
            );
        }

        return result;
    }
        
    private static double computeAverageSimilarity(
            Map<String, double[]> features
    ) {

        List<String> names =
                new ArrayList<>(
                        features.keySet()
                );

        double sum = 0;
        int count = 0;

        for (
                int i = 0;
                i < names.size();
                i++
        ) {

            for (
                    int j = i + 1;
                    j < names.size();
                    j++
            ) {

                double sim =
                        cosine(
                                features.get(names.get(i)),
                                features.get(names.get(j))
                        );

                sum += sim;
                count++;
            }
        }

        return count == 0
                ? 0
                : sum / count;
    }

    private static double cosine(
            double[] a,
            double[] b
    ) {

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {

            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0)
            return 0;

        return dot /
                (
                        Math.sqrt(normA)
                                *
                                Math.sqrt(normB)
                );
    }
}
