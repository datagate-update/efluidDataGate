-- Script de MAJ de BDD démo pour le sample de génération - ORACLE 12c

INSERT INTO "TCATEGORY"("NAME", "CODE") VALUES ('New Item', 'Test test');
INSERT INTO "TMATERIEL"("DETAILS", "SERIAL", "CAT_ID") VALUES ('Central à biogaz', 'PIGEON001', 5);
INSERT INTO "TTYPECOMPTEURS"("DESIGNATION", "VARIANTE") VALUES ('Vieu compteur jaune', 'Jaune');
INSERT INTO "TTYPECOMPTEURS"("DESIGNATION", "VARIANTE") VALUES ('Vieu compteur bleu', 'Bleu');
INSERT INTO "TTYPECOMPTEURS"("DESIGNATION", "VARIANTE") VALUES ('Vieu compteur rose', 'Rose');
INSERT INTO "TTYPECOMPTEURS"("DESIGNATION", "VARIANTE") VALUES ('Vieu compteur vert', 'Vert');
INSERT INTO "TCOMPTEUR"("DETAILS", "SERIAL", "NOM", "CAT_ID", "FABRIQUANT", "ACTIF", "TYPE_ID" ) VALUES ('Compteur cassé 111', 'CASSAAAA', 'C001111', 1, 'Shneider', 1 ,1);
INSERT INTO "TCOMPTEUR"("DETAILS", "SERIAL", "NOM", "CAT_ID", "FABRIQUANT", "ACTIF", "TYPE_ID" ) VALUES ('Compteur cassé 222', 'CASSBBBB', 'C002222', 2, 'Shneider', 0 ,1);
INSERT INTO "TCOMPTEUR"("DETAILS", "SERIAL", "NOM", "CAT_ID", "FABRIQUANT", "ACTIF", "TYPE_ID" ) VALUES ('Compteur cassé 333', 'CASSCCCC', 'C003333', 2, 'Shneider', 1 ,2);
