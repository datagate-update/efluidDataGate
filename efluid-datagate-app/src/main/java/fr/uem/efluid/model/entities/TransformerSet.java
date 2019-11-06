package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * When a transformer source is defined and some transformer elements can be associated to
 * a commit process, they are packaged as a "transformer set" : it's like a "bag" with all
 * source values which <i>may</i> be required with associated transformer components when
 * a commit content is imported
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
@Entity
@Table(name = "tansformer_sets")
public class TransformerSet implements Shared {

	@Id
	private UUID uuid;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private TransformerSource source;

	@Column(name = "hashv")
	private String hash;

	private LocalDateTime importedTime;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JoinColumn(name = "set_uuid")
	private Collection<TransformerSetEntry> entries = new ArrayList<>();

	/**
	 * @return the uuid
	 */
	@Override
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the source
	 */
	public TransformerSource getSource() {
		return this.source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(TransformerSource source) {
		this.source = source;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the importedTime
	 */
	@Override
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 *            the importedTime to set
	 */
	public void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
	}

	/**
	 * @return the entries
	 */
	public Collection<TransformerSetEntry> getEntries() {
		return this.entries;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(Collection<TransformerSetEntry> entries) {
		this.entries = entries;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public String serialize() {

		// Always includes entries as sub item
		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("src", getSource().getUuid())
				.with("has", getHash())
				.with("ets", getEntries().stream().map(TransformerSetEntry::serialize).collect(Collectors.joining("\n")))
				.toString();
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("uid", this::setUuid)
				.applyUUID("src", u -> setSource(new TransformerSource(u)))
				.applyString("has", this::setHash)
				.applyString("ets", i -> {
					// Empty => Empty idx list
					if ("".equals(i)) {
						// No content
					}

					// Process content
					else {
						setEntries(Stream.of(i.split("\n")).map(s -> {
							TransformerSetEntry ent = new TransformerSetEntry();
							ent.deserialize(s);
							return ent;
						}).collect(Collectors.toList()));
					}
				});
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.hash == null) ? 0 : this.hash.hashCode());
		result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransformerSet other = (TransformerSet) obj;
		if (this.hash == null) {
			if (other.hash != null)
				return false;
		} else if (!this.hash.equals(other.hash))
			return false;
		if (this.uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!this.uuid.equals(other.uuid))
			return false;
		return true;
	}

}
