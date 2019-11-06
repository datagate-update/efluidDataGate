package fr.uem.efluid;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

/**
 * <p>
 * Specification of one transformer inside a project. A transformer is a source table and
 * a component classname. The specified component is of type "Transformer" and is
 * available only into Datagate application. So its name is specified in string value. It
 * can be usefull to define the standard transformers for one application usage in
 * constant values
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
@Documented
@Inherited
@Retention(RUNTIME)
public @interface ParameterTransformer {

	/**
	 * <p>
	 * Transformer source table name
	 * </p>
	 * 
	 * @return
	 */
	String table();

	/**
	 * <p>
	 * Transformer component which can use content from source to extract transformer set
	 * package and apply transformation to a commit content
	 * </p>
	 * 
	 * @return
	 */
	String transformerClass();

	/**
	 * <p>
	 * A transformer may need a configuration. Set it here. Format is transformer-specific
	 * </p>
	 * 
	 * @return
	 */
	String transformerConfig() default "";
}
