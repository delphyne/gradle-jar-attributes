package com.github.delphyne.gradle.plugins.jar_attributes

import org.gradle.api.internal.project.AbstractProject
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
class JarAttributesExtensionTest {

	AbstractProject p

	@BeforeMethod
	public void setup() {
		p = (AbstractProject) ProjectBuilder.builder().build()
	}

	public void testAtributes() {
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p, false)
		p.jarAttributes {
			applyDefaults false
			normalVal 1
			closureVal { 2 }
			setVal = 24
			setClosure = { 42 }
			'key-with-dashes' "hello, world"
		}
		p.evaluate()
		def r = extension.resolve()
		assert r.normalVal == "1"
		assert r.closureVal == "2"
		assert r.setVal == "24"
		assert r.setClosure == "42"
		assert r['key-with-dashes'] == "hello, world"
	}

	public void testEnv() {
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p, false)
		def existingEnvVar = System.getenv().keySet().head()
		p.jarAttributes {
			'built-by' env(existingEnvVar)
			'missing-env' env('IAMAENVIRONMENTVARIABLEWHICHISEXTREMELYUNLIKELYTOEXISTOK')
		}
		p.evaluate()
		def r = extension.resolve()
		assert r['built-by'] == System.getenv(existingEnvVar)
		assert !r.containsKey('missing-env')
	}

	public void testGradleProperties() {
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p, false)
		p.afterEvaluate {
			p.ext.foo = 'bar'
		}
		p.jarAttributes {
			'late-property' prop('foo')
			'missing-prop' prop('IAMAPROPERTYWHICHISEXTREMELYUNLIKELYTOEXISTOK')
		}
		p.evaluate()
		def r = extension.resolve()
		assert r['late-property'] == 'bar'
		assert !r.containsKey('missing-prop')
	}

	public void testSystemProperties() {
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p, false)
		p.jarAttributes {
			'system-property' sysprop('java.version')
			'missing-sysprop' syprop('IAMASYSTEMPROPERTYWHICHISEXTREMELYUNLIKELYTOEXISTOK')
		}
		p.evaluate()
		def r = extension.resolve()
		assert r['system-property'] == System.properties['java.version']
		assert !r.containsKey('mising-sysprop')
	}

	public void testDefaults() {
		def name = 'JarAttributesExtensionTest'
		def group = 'com.github.delphyne'
		def version = '1.2.2-SHAPSHOT'
		AbstractProject p = (AbstractProject) ProjectBuilder.builder().withName(name).build()
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p)
		p.group = group
		p.version = version
		p.evaluate()
		def r = extension.resolve()
		assert r.size() == 3
		assert r['Implementation-Title'] == name
		assert r['Implementation-Vendor'] == group
		assert r['Implementation-Version'] == version
	}

	public void testDefaultsDisabled() {
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p)
		p.jarAttributes {
			applyDefaults false
		}
		p.evaluate()
		assert extension.resolve().isEmpty()
	}

	public void testDefaultsOverridden() {
		def title = 'AnotherProjectName'
		def vendor = 'Acme, Inc.'
		def version = '0.0.1'
		AbstractProject p = (AbstractProject) ProjectBuilder.builder().withName('JarAttributesExtensionTest').build()
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p)
		p.jarAttributes {
			'Implementation-Title' title
			'Implementation-Vendor' vendor
			'Implementation-Version' version
		}
		p.group = 'com.github.delphyne'
		p.version = '1.2.2-SHAPSHOT'
		p.evaluate()
		def r = extension.resolve()
		assert r.size() == 3
		assert r['Implementation-Title'] == title
		assert r['Implementation-Vendor'] == vendor
		assert r['Implementation-Version'] == version
	}

	public void testDefaultsRemoved() {
		def name = 'JarAttributesExtensionTest'
		def group = 'com.github.delphyne'
		def version = '1.2.2-SHAPSHOT'
		AbstractProject p = (AbstractProject) ProjectBuilder.builder().withName(name).build()
		def extension = p.extensions.create('jarAttributes', JarAttributesExtension, p)
		p.group = group
		p.version = version
		p.jarAttributes {
			'Implementation-Version' null
		}
		p.evaluate()
		def r = extension.resolve()
		assert r.size() == 2
		assert r['Implementation-Title'] == name
		assert r['Implementation-Vendor'] == group
	}
}
