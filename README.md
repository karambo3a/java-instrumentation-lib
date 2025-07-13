# Java Instrumentation Library

A bytecode instrumentation library designed for line/branch coverage tracking and fuzzing of Java applications.

* Runtime classfile transformations using Java Agent API and Class-File API
* Flexible package/class filtering
* Agent configuration at JVM launch

### Coverage tracking

* Tracks visited lines (line coverage)
* Monitors branch execution (branch coverage)
* Configurable unique/non-unique coverage tracking

### Fuzzing

The data used in the program is collected to generate the program input:

* Extracts numeric literals from branch conditions
* Collects array access indices

## Installation

### Build

```bash
./gradlew instrumentation:agentJar
```

Built files location: `instrumentation/build/libs`.

### Usage

Move `coverage-agent-1.0-SNAPSHOT-agent.jar` to your project.

**Important**: Java Class-File API needs `--enable-preview`.

#### Gradle

```groovy
tasks.register('run', JavaExec) {

    group = 'Application'

    setMainClass('MainClass')

    classpath = sourceSets.main.runtimeClasspath

    doFirst {
        def agentJar = 'path/to/coverage-agent-1.0-SNAPSHOT-agent.jar'
        jvmArgs += [
                '-javaagent:' + agentJar + '=isUnique=true|false,{classRegex},{metrics}',
                '--enable-preview'
        ]
    }
}
```

#### Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <argLine>
            -javaagent:'path/to/coverage-agent-1.0-SNAPSHOT-agent.jar'=isUnique=true|false,{classRegex},{metrics}
            --enable-preview
        </argLine>
    </configuration>
</plugin>
```

#### About parameters

|Parameter|Description|Valid values|
|----------|------------|-------|
|isUnique|Coverage tracking is unique or not|'true' or 'false'|
|classRegex|Regex pattern for classes to instrument|Java regex pattern|
|metrics|Metrics to collect|Any combinations of 'line', 'branch', 'constant', 'indices'|

For example,

```groovy
'-javaagent:' + agentJar + '=isUnique=true,com/myapp/.*,branch,constant'
```
