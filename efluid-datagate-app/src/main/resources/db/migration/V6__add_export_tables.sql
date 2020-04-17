-- Add table for management of export content with transformerDef customization

CREATE TABLE "EXPORTS" (
    "UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"FILENAME" VARCHAR2(255 CHAR),
	"CREATED_TIME" TIMESTAMP (6) NOT NULL ENABLE,
	"DOWNLOADED_TIME" TIMESTAMP (6),
	"PROJECT_UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"START_COMMIT_UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"END_COMMIT_UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 PRIMARY KEY ("UUID") ENABLE,
	 CONSTRAINT "EXPORT_PROJECT" FOREIGN KEY ("PROJECT_UUID") REFERENCES "PROJECTS" ("UUID") ENABLE,
	 CONSTRAINT "EXPORT_END_COMMIT" FOREIGN KEY ("END_COMMIT_UUID") REFERENCES "COMMITS" ("UUID") ENABLE,
	 CONSTRAINT "EXPORT_START_COMMIT" FOREIGN KEY ("START_COMMIT_UUID") REFERENCES "COMMITS" ("UUID") ENABLE
);

CREATE TABLE "EXPORT_TRANSFORMERS" (
    "ID" NUMBER(19,0),
	"TRANSFORMER_DEF_UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"EXPORT_UUID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CONFIGURATION" CLOB,
	 PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "EXPORT_TR_TRANSFORMER" FOREIGN KEY ("TRANSFORMER_DEF_UUID") REFERENCES "TRANSFORMERS" ("UUID") ENABLE,
	 CONSTRAINT "EXPORT_TR_EXPORT" FOREIGN KEY ("EXPORT_UUID") REFERENCES "EXPORTS" ("UUID") ENABLE
);