BEGIN;

CREATE TABLE GROUP_PRINCIPAL (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  UID varchar(12) NOT NULL,
  ENVIRONMENT_ID bigint(20) NOT NULL,
  STATUS int(11) NOT NULL,
  GROUP_ID bigint(20) NOT NULL,
  PRINCIPAL_ID bigint(20) NOT NULL DEFAULT '0',
  PRINCIPAL_UID varchar(12) NOT NULL DEFAULT '',
  PRINCIPAL_TYPE varchar(5) NOT NULL DEFAULT '',
  CREATED datetime DEFAULT NULL,
  MODIFIED datetime DEFAULT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY UID (UID)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE GROUP_PRINCIPAL ADD INDEX ENVIRONMENT_ID_IND USING BTREE(ENVIRONMENT_ID);
ALTER TABLE GROUP_PRINCIPAL ADD INDEX GROUP_ID_IND USING BTREE(GROUP_ID);
ALTER TABLE GROUP_PRINCIPAL ADD INDEX PRINCIPAL_UID_IND USING BTREE(PRINCIPAL_UID);
ALTER TABLE GROUP_PRINCIPAL ADD INDEX PRINCIPAL_TYPE_IND USING BTREE(PRINCIPAL_TYPE);

COMMIT;