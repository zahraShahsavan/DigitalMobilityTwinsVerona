# Verona Mobility Twin

## A Distributed Geospatial Digital Twin for Multi-Modal Urban Mobility Analysis and Similar-District Retrieval, Using Spark to store and process data


# Project Overview

This project builds a lightweight **urban mobility digital twin of Verona** using Java and Apache Spark.

The system combines several public geospatial datasets from Verona and generates:

* a final **PNG map visualization** of Verona
* district boundaries
* school locations
* tourist attraction points
* district similarity groups based on K-nearest neighbors (KNN)
* a validation report summarizing similarity metrics and runtime performance

Districts with similar mobility and urban characteristics are highlighted using the same border color on the final map.

---

# Project Structure

```bash
mobilityTwins/
│
├── src/main/java/verona/mobilityTwins/
│   ├── App.java
│   ├── ValidationReport.java
│
├── data/
│   ├── worldcover.tif
│   ├── CS_OPENDATA_QUARTIERI_polygon.shp
│   ├── SIGI 180 scuole.shp
│   ├── veronacard_2020_opendata.csv
│   ├── features.csv
│
├── output/
│   ├── verona_final_map.png
│   ├── validation_report.txt
│
├── pom.xml
└── README.md
```

---

# Datasets Used

The project uses the following public datasets:

* Quartieri boundaries – Comune di Verona
* Schools dataset – Comune di Verona
* VeronaCard 2020 tourist attractions
* Population dataset
* Vehicle circulation dataset
* Numerazione Civica dataset
* Parks and green areas dataset
* ESA WorldCover GeoTIFF raster background

---

# Software Requirements

Before running the project install:

* Java 11 or newer
* Apache Maven
* Apache Spark 3.x
* BEAST spatial library
* GeoTools dependencies

---

# How to Compile

Open terminal inside the project root directory:

```bash
mvn clean package
```

This compiles the project and downloads required dependencies.

---

# How to Run

Run the application with:

```bash
mvn exec:java -Dexec.mainClass="verona.mobilityTwins.App"

---

# Output Files

After execution the project generates:

### 1. Final Map

```bash
output/verona_final_map.png
```

Contains:

* Verona district polygons
* district borders colored by KNN similarity
* school points
* WorldCover geographic background raster

---

### 2. Validation Report

```bash
output/validation_report.txt
```

Contains:

* number of districts
* similarity metrics
* nearest-neighbor results
* cluster distribution
* runtime performance
* interpretation of results

---

# KNN Similarity

K-nearest neighbors is applied on:

```bash
data/features.csv
```

Each district is represented by:

* tourist_visits
* vehicles
* schools
* parks
* roads
* vehicle_index
* bike_index
* pedestrian_index

KNN computes cosine similarity between districts and retrieves the top similar neighbors.

These results are used to color district borders on the final PNG map.


---
