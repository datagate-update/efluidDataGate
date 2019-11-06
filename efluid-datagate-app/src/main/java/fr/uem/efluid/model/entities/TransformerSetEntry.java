package fr.uem.efluid.model.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
@Entity
@Table(name = "tansformer_data")
public class TransformerSetEntry {

	@Id
	@GeneratedValue
	private Long id;

	private String payload;

	@ManyToOne(optional = false)
	private TransformerSet transformerSet;

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return this.payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * @return the transformerSet
	 */
	public TransformerSet getTransformerSet() {
		return this.transformerSet;
	}

	/**
	 * @param transformerSet
	 *            the transformerSet to set
	 */
	public void setTransformerSet(TransformerSet transformerSet) {
		this.transformerSet = transformerSet;
	}

	/**
	 * For set serialize
	 * 
	 * @return
	 */
	String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("pay", getPayload())
				.toString();
	}

	/**
	 * For set deserialize
	 * 
	 * @param raw
	 */
	void deserialize(String raw) {
		SharedOutputInputUtils.fromJson(raw)
				.applyString("pay", p -> setPayload(p));
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.payload == null) ? 0 : this.payload.hashCode());
		result = prime * result + ((this.transformerSet == null) ? 0 : this.transformerSet.hashCode());
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
		TransformerSetEntry other = (TransformerSetEntry) obj;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.payload == null) {
			if (other.payload != null)
				return false;
		} else if (!this.payload.equals(other.payload))
			return false;
		if (this.transformerSet == null) {
			if (other.transformerSet != null)
				return false;
		} else if (!this.transformerSet.equals(other.transformerSet))
			return false;
		return true;
	}

}
