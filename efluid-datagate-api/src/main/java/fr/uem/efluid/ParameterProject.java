package fr.uem.efluid;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

/**
 * <p>
 * Specification for active project in current package / meta annotation. The specified
 * transformer sources are also specified directly at project level
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 2
 */
@Documented
@Inherited
@Retention(RUNTIME)
public @interface ParameterProject {

	/**
	 * <p>
	 * Mandatory spec of the project name. Will be created with this exact name
	 * </p>
	 * <p>
	 * Note than in some cases the name can be used to identify an item in application
	 * </p>
	 * 
	 * @return
	 */
	String name();

	/**
	 * <p>
	 * Predefined color identity for the project
	 * </p>
	 * 
	 * @return
	 */
	ProjectColor color();

	/**
	 * <p>
	 * Optional specified transformers for the project. Transformers are dictionary items
	 * specified directly at the project level
	 * </p>
	 * 
	 * @return
	 */
	ParameterTransformer[] transformers() default {};
}
