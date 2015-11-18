package com.github.delphyne.gradle.plugins.jar_attributes

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * A plugin which simplifies the addition of attributes to your project's Jar manifests.  In particular, this plugin:
 * <ul>
 *     <li>provides defaults for the common attributes which may be determined from your build script</li>
 *     <li>provides a dsl to set attrs to the value of an Environmental variable</li>
 *     <li>provides a dsl to set attrs to the value of a build property</li>
 *     <li>filters (and logs as debug) null values</li>
 * </ul>
 */
class JarAttributesPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		/*
		 * Register our extension object
		 */
		project.extensions.create(JarAttributesExtension.NAME, JarAttributesExtension, project)

		project.afterEvaluate {
			JarAttributesExtension extension = project.extensions.findByType(JarAttributesExtension)
			project.tasks.withType(Jar).each { Jar jar ->
				jar.manifest.attributes(extension.resolve())
			}
		}
	}
}
