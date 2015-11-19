package com.github.delphyne.gradle.plugins.jar_attributes

import org.gradle.api.Project

/**
 * Configuration for the {@link JarAttributesPlugin}.  Configuration is done via builder syntax:
 *
 * {code}
 * jarAttributes &#123;
 *   applyDefaults false
 *   foo "bar"
 *   baz true
 *   someCalculatedValue = &#123; Math.random() &#125;
 * &#125;
 * {code}
 *
 * Will create a manifest with the following entries:
 * foo: bar
 * baz: true
 * someCalculatedValue: -0.1234442112
 *
 */
class JarAttributesExtension {

	final static NAME = 'jarAttributes'

	/**
	 * If true (default), adds the following entries to the manifest:
	 *
	 * Implementation-Title: project.name
	 * Implementation-Vendor: project.group
	 * Implementation-Version: project.version
	 *
	 * Any values provided manually override these values.  To remove a value but get other defaults, add it with a null:
	 *
	 * {code}
	 * jarAttributes &#123;
	 *   'Implementation-Vendor' null
	 * &#125;
	 * {code}
	 */
	boolean applyDefaults
	final Map<String, String> attributes = [:]
	final Project project

	final static def defaultAttributes = [
			'Implementation-Title': { Project project -> project.name },
			'Implementation-Vendor': { Project project -> project.group },
			'Implementation-Version': { Project project -> project.version }
	].asImmutable()

	JarAttributesExtension(Project project, boolean applyDefaults = true) {
		this.project = project
		this.applyDefaults = applyDefaults
	}

	/**
	 * Method to allow setting {@link #applyDefaults} in a builder syntax.
	 */
	JarAttributesExtension applyDefaults(boolean applyDefaults) {
		this.applyDefaults = applyDefaults
		this
	}

	/**
	 * Get provided name as an environment variable.  Environment Variables are applied lazily after project evaluation.
	 * For example:
	 *
	 * {code}
	 * jarAttributes &#123;
	 *   'built-by' env('USER')
	 * &#125;
	 * {code}
	 */
	@SuppressWarnings("GrMethodMayBeStatic")
	Closure<String> env(String varName) {
		return { System.getenv(varName) }
	}

	/**
	 * Get provided name as a project property.  Properties are applied lazily after project evaluation (properties
	 * provided by other plugins should be available).  For example:
	 *
	 * {code}
	 * jarAttributes &#123;
	 *   'build-file' gprop('buildFile')
	 * &#125;
	 * {code}
	 */
	Closure<String> prop(String propName) {
		return { project.properties[propName]?.toString() }
	}

	/**
	 * Get provided name as a project property.  Properties are applied lazily after project evaluation (properties
	 * provided by other plugins should be available).  For example:
	 *
	 * {code}
	 * jarAttributes &#123;
	 *   'build-file' sprop('buildFile')
	 * &#125;
	 * {code}
	 */
	@SuppressWarnings("GrMethodMayBeStatic")
	Closure<String> sysprop(String propName) {
		return { System.properties[propName].toString() }
	}


	/**
	 * Method to allow lookup of dynamically generated attributes.
	 */
	def propertyMissing(String name) {
		attributes[name]
	}

	/**
	 * Method to allow property-like syntax to define attributes.
	 */
	def propertyMissing(String name, value) {
		methodMissing(name, [value])
	}

	/**
	 * Method to allow builder syntax to define attributes.
	 */
	def methodMissing(String name, args) {
		if (args.size() != 1) {
			throw new InvalidManifestAttributeException("Refusing to add ${name} to manifest because the provided arguments are invalid.  Excpected exactly one, but got ${args.size()}.  Actual: ${args as List}")
		}
		attributes[name] = args.head()
	}

	@SuppressWarnings("GrMethodMayBeStatic")
	public void update(Map<String, String> r, String k, def v) {
		if (v instanceof Closure) {
			v = v(project)
		}
		if (v == null) {
			project.logger.debug("Omitting '{}' from manifest due to null value.")
		}
		if (v != null) {
			r[k] = v.toString()
		}
	}

	/**
	 * Return the full set of attributes that will be returned.  Attributes are returned in a deterministic order.
	 * Defaults are returned first (if enabled), followed by any additional attributes.
	 */
	Map<String, String> resolve() {
		def r = [:]
		if (applyDefaults) {
			/*
			 * Scan through defaults, adding the default or any override in place
			 */
			defaultAttributes.each { k, v ->
				update(r, k, attributes.containsKey(k) ? attributes[k] : v)
			}
		}
		/*
		 * Add all manually configured attrs, skipping anything already added via defaults if enabled.
		 */
		attributes.each { k, v ->
			if (!applyDefaults || !defaultAttributes.containsKey(k)) {
				update(r, k, v)
			}
		}
		r
	}

	String toString() {
		"${getClass().simpleName}: ${[applyDefaults: applyDefaults, attributes: resolve()]}"
	}
}
