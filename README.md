# git-config

This project is aimed for those people that has more than 1 configuration for their 
git projects or work with multiple users and want to have a simple command to change
their profile between projects.

## Requirements

- Groovy 3.0.6
- GraalVM CE 20.3.0

```shell script
sdk install java 20.3.0.r8-grl
sdk install groovy 3.0.6
```

## Compile Groovy Script

A compiler configuration is included to properly compile with Static Compilation capabilities
```
withConfig(configuration) {
    ast(groovy.transform.CompileStatic)
    ast(groovy.transform.TypeChecked)
}
```

Now in order to use this configuration at compile time we need to run the following command

```shell script
groovyc --configscript compiler.groovy git-config.groovy
```

## Run with Java

You can run your script with Java by including in the classpath the Groovy jar file

```shell script
java -cp ".:$GROOVY_HOME/lib/groovy-3.0.6.jar:$GROOVY_HOME/lib/groovy-json-3.0.6.jar" git-config --help
```

## Build native-image

```shell script
native-image --allow-incomplete-classpath \
--report-unsupported-elements-at-runtime \
--initialize-at-build-time \
--initialize-at-run-time=org.codehaus.groovy.control.XStreamUtils,groovy.grape.GrapeIvy \
--no-server \
--no-fallback \
-cp ".:$GROOVY_HOME/lib/groovy-3.0.6.jar:$GROOVY_HOME/lib/groovy-json-3.0.6.jar" \
-H:ReflectionConfigurationFiles=conf/git-config-reflections.json,conf/java-reflections.json,conf/dgm-reflections.json \
git-config
```

### Sample output

```shell script
[git-config:3337]    classlist:   3,262.39 ms,  1.34 GB
[git-config:3337]        (cap):   6,552.59 ms,  1.34 GB
[git-config:3337]        setup:  10,432.12 ms,  1.34 GB
[git-config:3337]     (clinit):     357.15 ms,  2.19 GB
[git-config:3337]   (typeflow):   7,953.68 ms,  2.19 GB
[git-config:3337]    (objects):   8,057.15 ms,  2.19 GB
[git-config:3337]   (features):     538.85 ms,  2.19 GB
[git-config:3337]     analysis:  17,381.85 ms,  2.19 GB
[git-config:3337]     universe:     897.57 ms,  2.36 GB
[git-config:3337]      (parse):   1,673.26 ms,  2.36 GB
[git-config:3337]     (inline):   1,967.96 ms,  2.69 GB
[git-config:3337]    (compile):  11,607.48 ms,  3.33 GB
[git-config:3337]      compile:  16,448.78 ms,  3.33 GB
[git-config:3337]        image:   2,495.96 ms,  3.34 GB
[git-config:3337]        write:     653.93 ms,  3.34 GB
[git-config:3337]      [total]:  52,073.96 ms,  3.34 GB

```

### dgm reflections not recognized
If you have any problem with the dmg reflections not including a class or something you can run the 
tool included here

```groovy
groovy dgm-reflections-generator.groovy
```

After finish do not forget to include the file generated to your configuration `-H:ReflectionConfigurationFiles`.

Thanks to @wololock is published on his gist here:  https://gist.github.com/wololock/ac83a8196a8252fbbaacf4ac84e10b36

### Usage

You can see the options by running the command `-h` or `--help`

```shell script
./git-config -h

usage: git-config -[hvroi]
 -c,--configure     Configure user info
 -h,--help          Usage Information
 -p,--profile       Use profile configuration
 -s,--show          Show user info
 -sl,--show-local   Show local configuration

```

This tool has been written to change between git profiles that will be stored on json format on your
`${USER_HOME}/git-config/config.json` first you need to configure the tool by running the command

```shell script
./git-config -c
```

Follow the steps and then you are ready to change between your profiles using the next command:

```shell script
./git-config -p <name-of-your-profile>
```

You can see the configuration of your current directory by running the option `-sl` or `--show-local`
also you can see your current directory with the option `-s` or `--show`:

```shell script
./git-config -sl

Repo:  git@github.com:Joxebus/git-config-groovy-graalvm.git
Name:  Omar Bautista
Email: joxebus@gmail.com
```
