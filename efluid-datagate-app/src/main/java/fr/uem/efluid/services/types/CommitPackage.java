package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Commit;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitPackage extends SharedPackage<Commit> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public CommitPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	public String getVersion() {
		return "2";
	}

	/**
	 * @return
	 */
	@Override
	protected Commit initContent() {
		return new Commit();
	}

}
