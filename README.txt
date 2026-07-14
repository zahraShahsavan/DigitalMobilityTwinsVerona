# Verona Mobility Twin

## A Distributed Geospatial Digital Twin for Multi-Modal Urban Mobility Analysis and Similar-District Retrieval

## Project Overview

This project implements a lightweight **Urban Mobility Digital Twin** for the city of Verona using **Java** and **Apache Spark**. It integrates multiple public geospatial datasets to analyze urban mobility patterns and identify districts with similar characteristics using the **K-Nearest Neighbors (KNN)** algorithm.

The application generates:

- A high-resolution PNG map of Verona
- District boundaries
- School locations
- Tourist attraction points
- Similar district groups based on KNN
- A validation report containing similarity metrics and runtime statistics

Districts with similar mobility and urban characteristics are highlighted using the same border color on the final map.

---

## Project Structure

```text
mobilityTwins/
│
├── src/
│   └── main/
│       └── java/
│           └── verona/
│               └── mobilityTwins/
│                   ├── App.java
│                   └── ValidationReport.java
│
├── data/
│   ├── worldcover.tif
│   ├── CS_OPENDATA_QUARTIERI_polygon.shp
│   ├── SIGI 180 scuole.shp
│   ├── veronacard_2020_opendata.csv
│   └── features.csv
│
├── output/
│   ├── verona_final_map.png
│   └── validation_report.txt
│
├── pom.xml
└── README.md
```

---

## Datasets

The project uses publicly available datasets provided by the **Comune di Verona** and other open-data sources.

Included datasets:

- Verona district boundaries
- School locations
- VeronaCard 2020 tourist attractions
- Population data
- Vehicle circulation data
- Street numbering dataset
- Parks and green areas
- ESA WorldCover raster map

> **Note:** Download the ESA WorldCover raster and place it in the `data` directory with the filename:

```text
worldcover.tif
```

---

## Requirements

Before running the project, install:

- Java 11 or newer
- Apache Maven
- Apache Spark 3.x
- BEAST spatial library
- GeoTools

---

## Compile

From the project root directory, run:

```bash
mvn clean package
```

This command compiles the project and downloads all required dependencies.

---

## Run

Execute the application with:

```bash
mvn exec:java -Dexec.mainClass="verona.mobilityTwins.App"
```

---

## Output

### Final Map

```
output/verona_final_map.png
```

The generated map contains:

- Verona district polygons
- District borders colored according to KNN similarity
- School locations
- ESA WorldCover raster background

### Validation Report

```
output/validation_report.txt
```

The report includes:

- Number of districts
- Similarity metrics
- Nearest-neighbor results
- Cluster distribution
- Runtime performance
- Interpretation of the results

---

## K-Nearest Neighbors (KNN)

KNN is applied to the dataset:

```
data/features.csv
```

Each district is represented by several mobility and urban indicators, including:

- Tourist visits
- Vehicle traffic
- Number of schools
- Parks
- Road network
- Vehicle index
- Bicycle index
- Pedestrian index

Cosine similarity is used to identify the most similar districts. The resulting similarity groups are visualized by assigning the same border color to similar districts on the final map.

---

## Technologies

- Java
- Apache Spark
- Apache Maven
- GeoTools
- BEAST Spatial Library
- K-Nearest Neighbors (KNN)

---

## Results

The project demonstrates how a lightweight distributed geospatial digital twin can integrate heterogeneous urban datasets to support mobility analysis and identify districts with similar urban characteristics. The generated visualization and validation report provide an interpretable overview of the similarity analysis.
