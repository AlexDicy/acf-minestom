# ACF Minestom

## Usage
This an implementation of [ACF](https://github.com/aikar/commands) in [Minestom](https://github.com/Minestom/Minestom). 

### Gradle

```
repositories {
    maven { url 'https://jitpack.io' }
    maven { url 'https://repo.aikar.co/content/groups/aikar/' }
}
```
Add the Jitpack and Aikar repository
```
dependencies {
    compile 'com.github.AlexDicy:acf-minestom:v1.1-SNAPSHOT'
}
```
Add the dependency
```
plugins {
    id "com.github.johnrengelman.shadow" version "6.1.0"
}

shadowJar {
    relocate 'co.aikar.commands', '[YOUR PACKAGE]'
    relocate 'co.aikar.locales', '[YOUR PACKAGE]'
}

compileJava {
    options.compilerArgs += ["-parameters"]
    options.fork = true
    options.forkOptions.executable = 'javac'
}
```
Add the shadow plugin, relocation info, and compiler info

### Maven
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    <repository>
        <id>aikar</id>
        <url>https://repo.aikar.co/content/groups/aikar/</url>
    </repository>
</repositories>
```
Add the repositories
```
<dependencies>
    <dependency>
        <groupId>com.github.AlexDicy</groupId>
        <artifactId>acf-minestom</artifactId>
        <version>v1.1-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
Add the dependency
```
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.1.1</version>
            <configuration>
                <dependencyReducedPomLocation>${project.build.directory}/dependency-reduced-pom.xml</dependencyReducedPomLocation>
                <relocations>
                    <relocation>
                        <pattern>co.aikar.commands</pattern>
                        <shadedPattern>[YOUR PACKAGE]</shadedPattern>
                    </relocation>
                    <relocation>
                        <pattern>co.aikar.locales</pattern>
                        <shadedPattern>[YOUR PACKAGE]</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
Shade the dependency and relocate
```
<plugin>
    <version>3.7.0</version>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>[JAVA VERSION]</source>
        <target>[JAVA VERSION]</target>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```
You must enable -parameters on compiler plugin get proper argument names



