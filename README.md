[![Build Status](https://travis-ci.org/delphyne/gradle-jar-attributes.svg?branch=master)](https://travis-ci.org/delphyne/gradle-jar-attributes)

# gradle-jar-attributes
_A Gradle Plugin to manage your Jar's Manifests_

[Groovydoc](https://delphyne.github.io/.docs/delphyne/gradle-jar-attributes/)

The jar-attributes plugin simplifies the process of adding info to your jar's manifest.  It does this by automatically
omitting (and logging) attributes with null values.  This is particularly useful when you have a build that gets some
attributes for released artifacts from your CI server, yet you want to be able to build the jar locally for testing.

## Installation

### Within a standalone build.gradle
```groovy
apply plugin: 'com.github.delphyne.jar-attributes'

buildscript {
	repositories {
		maven {
			url 'https://delphyne.github.io/.m2/'
		}
	}
	dependencies {
		classpath 'com.github.delphyne:jar-attributes-gradle-plugin:0.0.1'
	}
}
```

### With a buildSrc directory
#### buildSrc/build.gradle
```groovy
repositories {
	maven {
		url 'https://delphyne.github.io/.m2/'
	}
}

dependencies {
	compile 'com.github.delphyne:jar-attributes-gradle-plugin:0.0.1'
}
```

#### build.gradle
```groovy
apply plugin: 'com.github.delphyne.jar-attributes'
```

## Usage

If you only want the default attributes, applying the plugin is sufficient.  If you wish to add your own attributes,
you can add dynamic values to the `jarAttributes` extension.

```groovy
jarAttributes {
    myAttribute "myAttributeValue"
}
```

### Flexible attribute definition style
You can use several configuration styles, within the limits of Groovy's capability to parse the input. 

For example:
```groovy
jarAttributes {
    foo 'bar'                 // dsl style
    foo = 'bar'               // property style
    foo { Math.random() }     // closure style
}
```


### Default Attributes

Out of the box, this plugin provides the 3 most common Jar Manifest Attributes:

1. `Implementation-Title` is set to project.name
2. `Implementation-Vendor` is set to project.group
3. `Implementation-Version` is set to project.version

Individual default attributes may be overridden by re-defining them in the jarAttributes extension.  If you redefine
any of the defaults to null, they will be omitted.

## DSL Helpers
DSL helpers are included to ease the use data injected from the build's runtime environment.  All of the DSL helpers
are invoked lazily and bound as late as possible in order to allow other plugins to provide the values you want to
inject.
 
### Gradle build properties
```groovy
jarAttributes {
    buildFile prop('buildFile')
}
```

### Java System Properties
```groovy
jarAttributes {
    jdkVersion sysprop('java.version')
}
```

### Environmental Variables
This is particularly helpful in CI environments
```groovy
jarAttributes {
    gitTag env('TRAVIS_TAG')
}
```

## Configuration

Name          | Type    | Default Value        | Description
--------------|---------|----------------------|-------------
applyDefaults | boolean | true                 | Whether or not to automatically apply the Implementation-{Title, Vendor, Version} attributes

### Limitations
```groovy
jarAttributes {
    'built-by' 'something'    // Attribute names which are not valid groovy 
                              // identifiers must be wrapped in quotes and
                              // must use the dsl or closure style invocations
}
```
