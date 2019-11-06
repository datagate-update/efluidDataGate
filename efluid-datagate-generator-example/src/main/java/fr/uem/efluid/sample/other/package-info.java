/**
 * <p>
 * For specified domain at package level
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@ParameterDomain(name = "Autre domaine", project = @ParameterProject(
		name = "My project",
		color = ProjectColor.BLUE,
		transformers = @ParameterTransformer(table = "regionalisation_data", transformerClass = "com.efluid.RegionalizationTransformer")))
package fr.uem.efluid.sample.other;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ParameterTransformer;
import fr.uem.efluid.ProjectColor;
