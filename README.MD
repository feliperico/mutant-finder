# Mutant Finder

## Instructions for Executing the Application
### Dependencies
MongoDB being executed on port 27017

### Execution Steps
Generate the jar
```
$ ./gradlew clean build
```

Start the application
```
$ java -jar build/libs/mutant-finder-0.0.1-SNAPSHOT.jar
```

## Endpoints

### POST /mutant
Request body:
```
{
"dna":["AAACCC",
       "GGGTTT",
       "CCCAAA",
       "TTTGGG",
       "ACACAC",
       "GTGTGT"]
}
```
Header
```
Content-Type: application/json 
```

### GET /stats
Header
```
Content-Type: application/json 
```
