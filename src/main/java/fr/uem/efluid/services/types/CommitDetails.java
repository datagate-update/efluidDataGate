package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitDetails {

	private UUID uuid;

	private String hash;

	private String originalUserEmail;

	private String comment;

	private LocalDateTime createdTime;

	private LocalDateTime importedTime;

	private List<UUID> mergeSources;

	private List<DiffDisplay<List<PreparedIndexEntry>>> content;

	private CommitState state;

	private CommitDetails() {
		super();
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	private void setUuid(UUID uuid) {
		this.uuid = uuid;
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
	private void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the originalUserEmail
	 */
	public String getOriginalUserEmail() {
		return this.originalUserEmail;
	}

	/**
	 * @param originalUserEmail
	 *            the originalUserEmail to set
	 */
	private void setOriginalUserEmail(String originalUserEmail) {
		this.originalUserEmail = originalUserEmail;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	private void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the createdTime
	 */
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
	}

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	private void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return the importedTime
	 */
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 *            the importedTime to set
	 */
	private void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
	}

	/**
	 * @return the mergeSources
	 */
	public List<UUID> getMergeSources() {
		return this.mergeSources;
	}

	/**
	 * @param mergeSources
	 *            the mergeSources to set
	 */
	private void setMergeSources(List<UUID> mergeSources) {
		this.mergeSources = mergeSources;
	}

	/**
	 * @return the content
	 */
	public List<DiffDisplay<List<PreparedIndexEntry>>> getContent() {
		return this.content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	private void setContent(List<DiffDisplay<List<PreparedIndexEntry>>> content) {
		this.content = content;
	}

	/**
	 * @return the state
	 */
	public CommitState getState() {
		return this.state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	private void setState(CommitState state) {
		this.state = state;
	}

	/**
	 * @return
	 */
	public boolean isEmptyDiff() {
		return this.content.stream().allMatch(d -> d.getDiff().isEmpty());
	}

	/**
	 * @param commit
	 * @return
	 */
	public static CommitDetails fromEntity(Commit commit) {

		CommitDetails details = new CommitDetails();

		details.setComment(commit.getComment());
		details.setCreatedTime(commit.getCreatedTime());
		details.setHash(commit.getHash());
		details.setImportedTime(commit.getImportedTime());
		details.setState(commit.getState());
		details.setOriginalUserEmail(commit.getOriginalUserEmail());
		details.setUuid(commit.getUuid());
		details.setMergeSources(commit.getMergeSources());

		// Using DiffDisplay for grouping index values
		details.setContent(commit.getIndex().stream()
				.map(PreparedIndexEntry::fromExistingEntity)
				.collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid))
				.entrySet().stream()
				.map(e -> {
					DiffDisplay<List<PreparedIndexEntry>> diff = new DiffDisplay<>();
					diff.setDictionaryEntryUuid(e.getKey());
					diff.setDiff(e.getValue());
					return diff;
				}).collect(Collectors.toList()));

		return details;
	}
}