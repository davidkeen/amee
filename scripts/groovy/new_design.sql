DROP TABLE IF EXISTS VALUE_DEFINITION;
CREATE TABLE IF NOT EXISTS VALUE_DEFINITION (
  ID int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
  NAME varchar(255) NOT NULL DEFAULT '',
  DESCRIPTION varchar(255) NOT NULL DEFAULT '',
  VALUE_TYPE tinyint(3) NOT NULL DEFAULT '0',
  CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (ID),
  UNIQUE KEY UID_KEY (UID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

INSERT INTO VALUE_DEFINITION (ID, UID, NAME, DESCRIPTION, VALUE_TYPE, CREATED, MODIFIED, STATUS) VALUES(1, '537270B92F6E', 'count', '', 4, '2007-07-27 08:30:44', '2007-07-27 08:30:44', 1);
INSERT INTO VALUE_DEFINITION (ID, UID, NAME, DESCRIPTION, VALUE_TYPE, CREATED, MODIFIED, STATUS) VALUES(2, '45433E48B39F', 'amount', '', 5, '2007-07-27 08:30:44', '2007-07-27 08:30:44', 1);
INSERT INTO VALUE_DEFINITION (ID, UID, NAME, DESCRIPTION, VALUE_TYPE, CREATED, MODIFIED, STATUS) VALUES(3, 'CCEB59CACE1B', 'text', '', 1, '2007-07-27 08:30:44', '2007-07-27 08:30:44', 1);
INSERT INTO VALUE_DEFINITION (ID, UID, NAME, DESCRIPTION, VALUE_TYPE, CREATED, MODIFIED, STATUS) VALUES(4, '996AE5477B3F', 'kgCO2PerKm', '', 5, '2007-07-27 08:30:44', '2007-07-27 08:30:44', 1);

DROP TABLE IF EXISTS ITEM_DEFINITION;
CREATE TABLE IF NOT EXISTS ITEM_DEFINITION (
 ID int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
 UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
 NAME varchar(255) NOT NULL DEFAULT '',
 DRILL_DOWN varchar(255) NOT NULL DEFAULT '',
 CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
 MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
 STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
 PRIMARY KEY (ID),
 UNIQUE KEY UID_KEY (UID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS ITEM_VALUE_DEFINITION;
CREATE TABLE IF NOT EXISTS ITEM_VALUE_DEFINITION (
  ID int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
  NAME varchar(255) NOT NULL DEFAULT '',
  PATH varchar(255) NOT NULL DEFAULT '',
  VALUE varchar(255) NOT NULL DEFAULT '',
  CHOICES varchar(255) NOT NULL DEFAULT '',
  FROM_PROFILE bit(1) NOT NULL DEFAULT b'0',
  FROM_DATA bit(1) NOT NULL DEFAULT b'0',
  ALLOWED_ROLES varchar(255) NOT NULL DEFAULT '',
  CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  ITEM_DEFINITION_ID int(11) UNSIGNED NULL DEFAULT NULL,
  VALUE_DEFINITION_ID int(11) UNSIGNED NULL DEFAULT NULL,
  UNIT varchar(255) NOT NULL DEFAULT '',
  PER_UNIT varchar(255) NOT NULL DEFAULT '',
  ALIASED_TO_ID int(11) UNSIGNED NULL DEFAULT NULL,
  STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
  FORCE_TIMESERIES bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (ID),
  UNIQUE KEY UID_KEY (UID),
  KEY FROM_PROFILE_KEY (FROM_PROFILE,FROM_DATA),
  KEY PATH_KEY (PATH),
  KEY ITEM_DEFINITION_ID_KEY (ITEM_DEFINITION_ID),
  KEY VALUE_DEFINITION_ID_KEY (VALUE_DEFINITION_ID),
  KEY ALIASED_TO_KEY (ALIASED_TO_ID) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

DROP TABLE IF EXISTS DATA_CATEGORY;
CREATE TABLE IF NOT EXISTS DATA_CATEGORY (
 ID int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
 UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
 NAME varchar(255) NOT NULL DEFAULT '',
 PATH varchar(255) NOT NULL DEFAULT '',
 CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
 MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
 DATA_CATEGORY_ID int(11) UNSIGNED NULL DEFAULT NULL,
 ITEM_DEFINITION_ID int(11) UNSIGNED NULL DEFAULT NULL,
 STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
 ALIASED_TO_ID int(11) UNSIGNED NULL DEFAULT NULL,
 WIKI_NAME varchar(255) NOT NULL DEFAULT '',
 PRIMARY KEY (ID),
 UNIQUE KEY UID_KEY (UID),
 KEY PATH_KEY (PATH),
 KEY ITEM_DEFINITION_ID_KEY (ITEM_DEFINITION_ID),
 KEY DATA_CATEGORY_ID_KEY (DATA_CATEGORY_ID),
 KEY WIKI_NAME_KEY (WIKI_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

DROP TABLE IF EXISTS DATA_ITEM;
CREATE TABLE IF NOT EXISTS DATA_ITEM (
  ID bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
  NAME varchar(255) NOT NULL DEFAULT '',
  PATH varchar(255) NOT NULL DEFAULT '',
  CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  ITEM_DEFINITION_ID int(11) UNSIGNED NULL DEFAULT NULL,
  DATA_CATEGORY_ID int(11) UNSIGNED NULL DEFAULT NULL,
  STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (ID),
  UNIQUE KEY UID_KEY (UID),
  KEY PATH_KEY (PATH),
  KEY ITEM_DEFINITION_ID_KEY (ITEM_DEFINITION_ID),
  KEY DATA_CATEGORY_ID_KEY (DATA_CATEGORY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

DROP TABLE IF EXISTS DATA_ITEM_NUMBER_VALUE;
CREATE TABLE IF NOT EXISTS DATA_ITEM_NUMBER_VALUE (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  UID char(12) NOT NULL DEFAULT '' COLLATE ascii_bin,
  STATUS tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
  VALUE DOUBLE NOT NULL DEFAULT '0.0',
  CREATED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  MODIFIED timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  ITEM_VALUE_DEFINITION_ID int(11) UNSIGNED NULL DEFAULT NULL,
  DATA_ITEM_ID bigint(20) UNSIGNED NULL DEFAULT NULL,
  UNIT varchar(255) NOT NULL DEFAULT '',
  PER_UNIT varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (ID),
  UNIQUE KEY UID_KEY (UID),
  KEY ITEM_VALUE_DEFINITION_ID_KEY (ITEM_VALUE_DEFINITION_ID),
  KEY DATA_ITEM_ID_KEY (DATA_ITEM_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;
