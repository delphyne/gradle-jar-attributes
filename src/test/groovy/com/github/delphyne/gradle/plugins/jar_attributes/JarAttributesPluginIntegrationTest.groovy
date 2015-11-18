package com.github.delphyne.gradle.plugins.jar_attributes

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.testng.annotations.Test

import java.util.jar.JarFile
import java.util.jar.Manifest

@Test
public class JarAttributesPluginIntegrationTest {
	public void testRealProject() {
		def name = "JarAttributesPluginIntegrationTest"
		def group = "com.github.delphyne.gradle.tests"
		def version = '0.0.1'
		File projectDir = File.createTempDir()
		def envKey = System.getenv().keySet()[new Random().nextInt(System.getenv().size())]
		def envVal = System.getenv(envKey)
		new File(projectDir, 'build.gradle').withWriter { BufferedWriter out ->
			out.print """
			apply plugin: 'java'
			apply plugin: ${JarAttributesPlugin.canonicalName}

			buildscript {
				dependencies {
					classpath files('${new File('.', 'build/classes/main').canonicalPath}')
				}
			}

			group='${group}'
			version='${version}'

			jarAttributes {
				'built-by' sysprop('user.name')
				'jdk-vendor' Runtime.class.package.implementationVendor
				'jdk-version' sysprop('java.version')
				'libs-dir' prop('libsDirName')
				randomEnv env('${envKey}')
			}
			"""
		}
		new File(projectDir, 'settings.gradle').withWriter { BufferedWriter out ->
			out.println "rootProject.name='${name}'"
		}
		GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir)
		ProjectConnection connection = connector.connect()
		BuildLauncher launcher = connection.newBuild()
		launcher.forTasks('jar')
		launcher.run()

		File jar = new File(projectDir, 'build/libs').listFiles().find { File f -> f.name.contains('.jar') }
		Manifest manifest = new JarFile(jar).manifest
		def attrs = manifest.attr
		assert attrs.getValue('built-by') == System.properties['user.name']
		assert attrs.getValue('jdk-vendor') == Runtime.class.package.implementationVendor
		assert attrs.getValue('jdk-version') == System.properties['java.version']
		assert attrs.getValue('libs-dir') == 'libs'
		assert attrs.getValue('randomEnv') == envVal
		assert attrs.getValue('Implementation-Title') == name
		assert attrs.getValue('Implementation-Vendor') == group
		assert attrs.getValue('Implementation-Version') == version
	}
}
