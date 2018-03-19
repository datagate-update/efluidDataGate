package fr.uem.efluid.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GeneratorMojo extends AbstractMojo implements DictionaryGeneratorConfig {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter
	private String sourcePackage;

	@Parameter
	private String destinationFolder;

	/**
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("###### Begin process for dictionary generation ######");

		try {
			DictionaryGenerator generator = new DictionaryGenerator(this);

			// Start generator using the classpath and custom logFacade
			generator.generateDictionaryExport(getMavenContextClassLoader());

		} catch (Exception e) {
			throw new MojoFailureException("Cannot process generation of client", e);
		}
	}

	/**
	 * <p>
	 * Dedicated init of classloader with access to all maven execution classpath, to
	 * allow the use of current compiled source + external dependencies during generation.
	 * </p>
	 * <p>
	 * We need to use the classpath from the project for class identification, including
	 * compiled sources from trans-connected project in IDE. So define a custom
	 * classloader with the maven project classpath.
	 * </p>
	 * <p>
	 * Some hacks are related to Eclipse M2E : Eclipse m2e is bullshit, and system is not
	 * compliant with maven project properties without custom m2e connector.
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	private ClassLoader getMavenContextClassLoader() throws Exception {

		ClassLoader contextClassLoader;

		// Hack from project param
		if (this.project != null) {

			Set<URL> urls = new HashSet<>();
			List<String> elements = this.project.getTestClasspathElements();

			for (String element : elements) {
				urls.add(new File(element).toURI().toURL());
			}

			contextClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

		}

		// Try anyway with current classpath
		else {
			contextClassLoader = this.getClass().getClassLoader().getParent();
		}

		Thread.currentThread().setContextClassLoader(contextClassLoader);

		return contextClassLoader;
	}

	/**
	 * @return the project
	 */
	public MavenProject getProject() {
		return this.project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(MavenProject project) {
		this.project = project;
	}

	/**
	 * @return the sourcePackage
	 */
	@Override
	public String getSourcePackage() {
		return this.sourcePackage;
	}

	/**
	 * @param sourcePackage
	 *            the sourcePackage to set
	 */
	public void setSourcePackage(String sourcePackage) {
		this.sourcePackage = sourcePackage;
	}

	/**
	 * @return the destinationFolder
	 */
	@Override
	public String getDestinationFolder() {
		return this.destinationFolder;
	}

	/**
	 * @param destinationFolder
	 *            the destinationFolder to set
	 */
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.generation.DictionaryGeneratorConfig#getLogger()
	 */
	@Override
	public LogFacade getLogger() {
		return new LogFacade() {

			@Override
			public void debug(CharSequence var1) {
				getLog().debug(var1);
			}

			@Override
			public void info(CharSequence var1) {
				getLog().info(var1);
			}

			@Override
			public void error(CharSequence var1, Throwable var2) {
				getLog().error(var1, var2);
			}
		};
	}
}
