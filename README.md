# Tutorial 6 - Logging with Log4j 2

This is the `MergeTransactions` project from
<https://github.com/boliu-christine/Tutorial6_Log4J> with all console output
replaced by Log4j 2 logging, as required by Tutorial 6.

---

## 1. What was changed

| File | Change |
|---|---|
| `pom.xml` | Added the two Log4j 2.26.1 dependencies (`log4j-api`, `log4j-core`) and the `exec-maven-plugin`. |
| `MergeTransactions.java` | Every `System.out` / `System.err` call and every `printStackTrace()` replaced by a Log4j 2 logger call. Two loggers are declared: `FILE` and `TRANSACTIONS`. |
| `src/main/resources/log4j2.xml` | **New.** The configuration: two named loggers, two appenders, `PatternLayout`. |
| `src/main/resources/log4j2-csv.xml` | **New.** Optional challenge: same loggers, but the file appender writes CSV. |
| `Purchase.java` | Unchanged. |

`log4j-api` is the only Log4j artefact the source code compiles against; the
implementation (`log4j-core`) is picked up at runtime. That is the separation of
concerns the tutorial is about: swapping appenders, layouts or levels never
requires touching the Java code.

---

## 2. Log levels

Each log call uses the level named in the comment that was already in the
original code:

| Original comment | Level | Where |
|---|---|---|
| `// print some info for the user` | `INFO` | summary: count, total value, max value |
| `// print info for user` | `INFO` | `import data from <file>` |
| `// this is for debugging only` | `DEBUG` | `imported transaction <purchase>` |
| `// print warning` | `WARN` | file does not exist - skip |
| `// print error message and details` | `ERROR` | parse failures, I/O failures, close failure |

---

## 3. The two loggers

| Logger | Concern | Writes to |
|---|---|---|
| `FILE` | accessing the transaction files | **console + `logs.txt`** |
| `TRANSACTIONS` | the transaction data (imported rows and the summary) | **console only** |

`logs.txt` therefore never contains a `TRANSACTIONS` line, and the transaction
data never ends up in the file-access log. Both use `PatternLayout`.

The console pattern is
`%d{HH:mm:ss.SSS} %-5level %-15.15c{1} - %msg%n`;
the file pattern additionally prints the full date, so successive runs stay
readable in `logs.txt`.

Stack traces are logged by passing the exception as the last argument
(`FILE_LOG.warn("file {} does not exist - skip", fileName, x)`), so the trace is
attached to the event instead of being printed separately to `System.err`.

---

## 4. How to run

From the project root (Maven 3.6+, JDK 11 or newer):

```
mvn compile exec:java -Dexec.mainClass=nz.ac.vuw.swen301.tuts.log4j.MergeTransactions
```

This reads `transactions1.csv` .. `transactions4.csv`, prints the log to the
console and writes `logs.txt` into the project root.

Expected summary (the data files contain two deliberate faults):

```
8 transactions imported
total value: $840.00
max value: $240.00
```

`transactions4.csv` does not exist, which produces the `WARN`;
`transactions3.csv` contains the malformed date `13/5/2018`, which produces the
`ERROR`.

### Changing the log level

Both loggers read their level from the system property `logLevel`, defaulting to
`debug`:

```
mvn compile exec:java -Dexec.mainClass=nz.ac.vuw.swen301.tuts.log4j.MergeTransactions -DlogLevel=info
mvn compile exec:java -Dexec.mainClass=nz.ac.vuw.swen301.tuts.log4j.MergeTransactions -DlogLevel=warn
```

* `debug` - everything, including the imported purchase rows
* `info`  - drops the per-transaction `DEBUG` lines
* `warn`  - only the `WARN` and `ERROR` events

### Character encoding

`PatternLayout` in both configuration files is pinned to `charset="UTF-8"`, so the
log files are identical on every platform. If your console mangles the JVM's own
exception messages, start Maven with UTF-8:

```
set MAVEN_OPTS=-Dfile.encoding=UTF-8
```

(On a Chinese Windows the JVM default is GBK.)

---

## 5. Optional challenge - CSV log file

`log4j2-csv.xml` writes `logs.csv`, one row per event:

```
2026-10-27 14:42:31.224,INFO,FILE,"import data from transactions1.csv"
2026-10-27 14:42:31.243,DEBUG,TRANSACTIONS,"imported transaction Purchase [retailer=Pack and Save, amount=188.0, ...]"
```

Columns: `timestamp, level, logger, message`. The message is quoted so the commas
inside it do not split the row, and `alwaysWriteExceptions="false"` keeps a stack
trace from spilling over several rows. Run it with:

```
mvn compile exec:java -Dexec.mainClass=nz.ac.vuw.swen301.tuts.log4j.MergeTransactions -Dlog4j2.configurationFile=src/main/resources/log4j2-csv.xml
```

The file opens directly in Excel, LibreOffice Calc or Google Sheets.

---

## 6. Project layout

```
Tutorial6_Log4J/
├── pom.xml
├── README.md
├── transactions1.csv
├── transactions2.csv
├── transactions3.csv
└── src/
    └── main/
        ├── java/nz/ac/vuw/swen301/tuts/log4j/
        │   ├── MergeTransactions.java
        │   └── Purchase.java
        └── resources/
            ├── log4j2.xml
            └── log4j2-csv.xml
```

`transactions4.csv` is intentionally absent - it triggers the `WARN` path.

`logs.txt` and `logs.csv` in the project root are **example outputs** from the
runs described above, included so the two-logger behaviour can be inspected
without building the project. Both are recreated (truncated) every time the
program runs, so they always reflect the most recent run.
