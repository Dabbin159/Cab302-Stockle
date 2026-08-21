# Stockle (Cab302-Stockle)

Stockle is a small Java modular application (student project) that provides a simulated stock-trading UI, data services, and tests. The source is a Maven-based Java project with a JavaFX UI located under `src/main/java`.
This was created in 13 weeks for QUT's CAB302 class.

## Prerequisites

- JDK 17 or newer
- Maven (optional) — the repository includes the Maven wrapper (`mvnw`, `mvnw.cmd`)
- JavaFX SDK (if running outside an IDE that already supplies JavaFX on the module path)

## Build

On Windows (using included wrapper):

```powershell
mvnw.cmd clean package
```

On macOS / Linux:

```bash
./mvnw clean package
```

## Run

Recommended: open the project in an IDE (IntelliJ IDEA, VS Code with Java extensions) and run the `com.stockle.StockleApplication` main class.

From the command line: running the UI may require JavaFX on the module path. If you have JavaFX installed, you can package and run with something like:

```bash
./mvnw package
java -p /path/to/javafx/lib --add-modules=javafx.controls,javafx.fxml -cp target/<artifact>.jar com.stockle.StockleApplication
```

Replace `/path/to/javafx/lib` and `target/<artifact>.jar` with your JavaFX library path and actual artifact name.

If the project is configured with the Maven Exec plugin, you may be able to run:

```bash
mvnw.cmd -Dexec.mainClass=com.stockle.StockleApplication exec:java
```

## Tests

Unit tests are under `src/test/java`. Run tests with:

```powershell
mvnw.cmd test
```

or

```bash
./mvnw test
```

## Project layout (important folders)

- `src/main/java` — application source (UI, services, models, database DAOs)
- `src/main/resources` — FXML and resource files
- `src/test/java` — unit tests

## Contributing

- Create an issue or open a PR on the repository.
- Keep changes small and focused; run tests locally before submitting.

## Notes

- The application uses the Java module system (`module-info.java`). Running from the command line may require correct module-path setup for JavaFX.
- If you want, I can add a small script or Maven plugin configuration to make running the application easier — tell me which OS and JavaFX setup you prefer.

## Team / Who Did What

- JJTSmasher (James) — Stock API integration, Stock data display
- Xicby (Ethan) — Database Management, Trading Logic
- Samlcamp (Sam) — Login System, Data Encryption
- jxn-1 (Jackson) — Front End Development
- Dabbin159 (Darien) — Project Manager, UI Functionality
- cstrange (Campbell) — AI API integration, Trading Logic

---

Created for the Cab302 assignment repository.
