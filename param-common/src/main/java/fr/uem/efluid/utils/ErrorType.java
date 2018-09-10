package fr.uem.efluid.utils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 10
 */
public enum ErrorType {

	OTHER,
	
	INIT_FAILED,
	
	WRONG_DS_TYPE,
	
	WRONG_CLIENT_CALL,
	
	PREPARATION_INTERRUPTED,
	PREPARATION_BIZ_FAILURE,
	PREPARATION_CANNOT_START,
	PREPARATION_NOT_READY,
	
	TABLE_NAME_INVALID,
	TABLE_WRONG_REF,
	
	UNSUPPORTED_UUID,
	
	COMMIT_IMPORT_INVALID,
	COMMIT_EXISTS,
	COMMIT_MISS_COMMENT,

	METADATA_WRONG_TYPE,
	METADATA_WRONG_SCHEMA,
	METADATA_FAILED,
	VALUE_CHECK_FAILED,
	VALUE_SHA_UNSUP,
	
	APPLY_FAILED,

	DIC_ENTRY_NOT_FOUND,
	DIC_NO_KEY,
	DIC_TOO_MANY_KEYS,
	DIC_NOT_REMOVABLE,
	DOMAIN_NOT_REMOVABLE,
	DIC_KEY_NOT_UNIQ,
	
	IMPORT_FAIL_FILE,
	EXPORT_FAIL_FILE,
	IMPORT_WRONG_TYPE,
	IMPORT_WRONG_VERSION,
	IMPORT_WRONG_INSTANCE,
	EXPORT_WRONG_APPEND,
	EXPORT_ZIP_FAILED,
	IMPORT_ZIP_FAILED,
	IMPORT_WRONG_READ,
	IMPORT_RUNNING,
	
	UPLOAD_WRG_DATA,
	
	TMP_ERROR,
	DATA_WRITE_ERROR,
	DATA_READ_ERROR,
	JSON_WRITE_ERROR,
	JSON_READ_ERROR,
	
	VERIFIED_APPLY_NOT_FOUND,
	
	REFER_MISS_LINK,
	
	PROJECT_MANDATORY,
	PROJECT_NAME_EXIST,
	
	VERSION_NOT_EXIST,
	VERSION_NOT_UP_TO_DATE,
	VERSION_NOT_IMPORTED,
	VERSION_NOT_MODEL_ID,
	OUTPUT_ERROR,
	
	ATTACHMENT_ERROR
	;

}
