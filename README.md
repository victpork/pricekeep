# PriceKeep

PriceKeep is an application that fetch products and prices from NZ-based supermarket websites. It also features graph to show the price trends and shows the lowest and averages.

# Technology stack

### Frontend: 
- TypeScript/vue.js/TailwindCSS
- Vite
- Shadcn/vue for the UI
- Orval (API library generation)

### Backend:
- Java 25
- Gradle (build tool)
- Quarkus (w/Hibernate ORM)
- PostgreSQL + PostGIS plugin (Data storage and location)
- Microsoft Playwright (For scraping)
- MapStruct (Object mapping)
- Quartz Scheduler (Run)

## Requirements for running dev environment locally
You'll need the following items pre-installed before starting
- Docker/Podman Desktop (For the devcontainers PostgreSQL)
- NodeJS

## Starting the dev environment

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

The one single command would help you to start both frontend and backend.

## Building a single package

The application can be packaged using:

```shell script
./gradlew build -x test
```

The application, packaged as jar, is located in `build/pricekeep-1.0.0-SNAPSHOT-runner.jar`. To run it, use the following command:
```shell script
java -jar pricekeep-1.0.0-SNAPSHOT-runner.jar
```

## Runtime Configuration
You may configure the database with `application.yaml` in the `config/` directorty located at the same place as the runner jar.
```
exercise-data/
├─ config
│ └─ application.yaml
└─ pricekeep-1.0.0-SNAPSHOT-runner.jar
```
Put the following in your application.yaml:
```yaml
quarkus:
  profile: staging

"%staging":
  quarkus:
    datasource:
      db-kind: postgresql
      jdbc:
        url: jdbc:postgresql://[your_server_address]:5433/pricekeep
      username: pricekeep
      password: [mypassword]
    flyway:
      migrate-at-start: true
```
