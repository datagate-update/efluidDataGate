-- Script de preparation de BDD avec référence entre table SANS UTILISER DE FK VALIDEES - ORACLE 12c

DROP TABLE "TREFERENCE";
DROP TABLE "TCONSUMER";

CREATE TABLE "TREFERENCE" (
	"KEY" VARCHAR2(256 BYTE) NOT NULL,
	"NAME" VARCHAR2(256 BYTE) NOT NULL, 
	"DETAIL" VARCHAR2(256 BYTE) NOT NULL,
    CONSTRAINT "TREFERENCE_KEY" PRIMARY KEY ("KEY") ENABLE
) TABLESPACE "USERS" ;

CREATE TABLE "TCONSUMER" (
	"ID" NUMBER(19,0) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	"CODE" VARCHAR2(256 BYTE) NOT NULL, 
	"VALUE" VARCHAR2(256 BYTE) NOT NULL,
	"VALUE_OTHER" VARCHAR2(256 BYTE) NOT NULL, 
	"REFERENCE_KEY" VARCHAR2(256 BYTE) NOT NULL,
    CONSTRAINT "TCONSUMER_pkey" PRIMARY KEY ("ID")
) TABLESPACE "USERS" ;

-- Pour l'instant des données très simples.

INSERT INTO "TREFERENCE" VALUES ('REF1', 'Reference Data 1', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF2', 'Reference Data 2', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF3', 'Reference Data 3', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF4', 'Reference Data 4', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF5', 'Reference Data 5', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');

INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_1' , 'Value 1', 'Value Other test', 'REF1');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_2' , 'Value 1', 'Value Other test', 'REF2');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_3' , 'Value 1', 'Value Other test', 'REF2');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_4' , 'Value 1', 'Value Other test', 'REF1');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_5' , 'Value 1', 'Value Other test', 'REF1');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_6' , 'Value 1', 'Value Other test', 'REF1');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_7' , 'Value 1', 'Value Other test', 'REF3');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_8' , 'Value 1', 'Value Other test', 'REF4');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_9' , 'Value 1', 'Value Other test', 'REF5');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_10', 'Value 1', 'Value Other test', 'REF6');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_11', 'Value 1', 'Value Other test', 'REF6');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_12', 'Value 1', 'Value Other test', 'REF6');

EXEC DBMS_STATS.gather_schema_stats('REFERENCE', cascade=>TRUE);