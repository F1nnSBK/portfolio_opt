# High-Performance Portfolio Optimizer 🚀

![Java](https://img.shields.io/badge/Java-25%20(Preview)-ed8b00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-6db33f?style=for-the-badge&logo=springboot&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Vector%20API%20%7C%20Virtual%20Threads-blue?style=for-the-badge)

> **Strategic Asset Allocation Engine** für das Planspiel Börse 2025.
> Entwickelt, um durch mathematische Überlegenheit (Markowitz Mean-Variance) und High-Performance Computing (SIMD) einen unfairen Vorteil zu erzielen.

## 🎯 Projekt-Ziel & Kontext

Dieses System wurde entwickelt, um ein Aktienportfolio mathematisch zu optimieren, anstatt sich auf Intuition zu verlassen. Es dient als **Decision Support System** für das "Planspiel Börse" und simuliert realistische Bedingungen des Asset Managements.

### Die "Planspiel"-Constraint (Regulatory Compliance)
Um realistische Diversifikation zu erzwingen (ähnlich zu UCITS-Richtlinien bei echten Fonds), implementiert der Algorithmus eine **harte Gewichtungsgrenze**:

* **Maximales Gewicht pro Position:** **20%**
* **Methodik:** Der Monte-Carlo-Solver nutzt **Rejection Sampling**, um nur solche Portfolios zu validieren, die diese regulatorische Anforderung erfüllen. Portfolios mit Klumpenrisiken (z.B. 60% NVIDIA) werden mathematisch verworfen.

## 🏗 Technology Stack (The "Steel Foundation")

Dieses Projekt nutzt **Bleeding Edge Java Features**, um maximale Performance auf Standard-Hardware zu erreichen.

* **Java 25 (Incubator Features):**
    * **Vector API (SIMD):** Berechnung der Kovarianz-Matrix nutzt CPU-Vektor-Instruktionen (AVX-512/AVX2) für massive Parallelisierung auf Hardware-Ebene.
    * **Virtual Threads (Project Loom):** Die Monte-Carlo-Simulation feuert **250.000 leichtgewichtige Threads** ab, ohne den OS-Kernel zu blockieren.
    * **Structured Concurrency:** Paralleler Datenabruf von externen APIs ohne "Thread Leaks".
* **Spring Boot 4.0.0:** Modernste Microservice-Architektur.
* **Data Provider:** Tiingo API (Adjusted Close Prices für korrekte Split/Dividenden-Berechnung).
* **Caching:** Caffeine In-Memory Cache für <50ms Latenz bei Folgeanfragen.

## 🧮 Mathematischer Kern

Der Optimizer basiert auf der **Modernen Portfoliotheorie (MPT)** nach Harry Markowitz.

1.  **Log-Returns:** $r_t = \ln(\frac{P_t}{P_{t-1}})$ für additive Eigenschaften und Normalverteilungs-Näherung.
2.  **Kovarianz-Matrix ($\Sigma$):** Berechnet mittels SIMD-optimiertem Dot-Product.
3.  **Simulation:** 250.000 Zufalls-Portfolios werden generiert, um die **Efficient Frontier** zu approximieren.
4.  **Zielfunktion:** Maximierung der **Sharpe Ratio**:
    $$S_p = \frac{R_p - R_f}{\sigma_p}$$
    *(Wobei $R_f = 3\%$ risikofreier Zins angenommen wird)*

## 🚀 Getting Started

### Prerequisites
* **JDK 25** (Early Access Build erforderlich für Vector API).
* **Tiingo API Token** (Kostenlos registrieren).

### Konfiguration
Setze den API Key in den Umgebungsvariablen oder in `application.properties`:
```properties
portfolio.provider.tiingo.key=DEIN_TOKEN
````

### RUN
Da die Vector API ein Incubator-Feature ist, muss die JVM mit speziellen Flags gestartet werden:
```
./gradlew bootRun
```
Hinweis: Die `build.gradle ist` bereits konfiguriert, um `--add-modules jdk.incubator.vector` an die JVM zu übergeben.

### Usage (API)
Das System exponiert eine REST-Schnittstelle.

### POST `/api/v1/portfolio/optimize`
```JSON
{
  "tickers": ["NVDA", "MSFT", "AAPL", "LVMUY", "URTH"]
}
```

### RESPONSE
```JSON

```

### 👨‍💻 Author
Finn Hertsch