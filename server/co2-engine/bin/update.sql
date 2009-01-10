# Fix Environments & Users
UPDATE amee.ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE amee.ENVIRONMENT SET OWNER = '';
UPDATE amee.USER SET LOCATION = '';
UPDATE amee.USER SET NICK_NAME = '';
UPDATE amee.USER SET USER_TYPE = 0;
UPDATE amee.USER SET USER_TYPE = 3 WHERE USERNAME = 'root';
ALTER TABLE amee.USER DROP COLUMN SUPER_USER;

# Fix Apps, Sites, etc.
UPDATE amee.APP SET TARGET_BUILDER = '';
UPDATE amee.SITE SET SERVER_ADDRESS = '', SERVER_PORT = '', SERVER_SCHEME = '', SECURE_AVAILABLE = 0;
DELETE FROM amee.TARGET WHERE TARGET='skinRenderResource';

# Fix Skins
DROP TABLE amee.SKIN_FILE;
DROP TABLE amee.SKIN_FILE_VERSION;
UPDATE amee.SKIN SET PATH = 'base-import' WHERE ID=1;
UPDATE amee.SKIN SET PATH = 'amee-base-import' WHERE ID=2;
UPDATE amee.SKIN SET PATH = 'admin-import' WHERE ID=3;
UPDATE amee.SKIN SET PATH = 'admin-default' WHERE ID=4;
update amee.SITE_APP set SKIN_PATH = 'admin-default' where SKIN_PATH = 'default.admin.skin';
UPDATE amee.SKIN SET PATH = 'app-admin' WHERE ID=5;
UPDATE amee.SITE_APP set SKIN_PATH = 'app-admin' WHERE SKIN_PATH = 'app.admin.skin';
UPDATE amee.SKIN SET PATH = 'cache-admin' WHERE ID=6;
UPDATE amee.SITE_APP set SKIN_PATH = 'cache-admin' WHERE SKIN_PATH = 'cache.skin';
UPDATE amee.SKIN SET PATH = 'site-admin' WHERE ID=7;
UPDATE amee.SITE_APP set SKIN_PATH = 'site-admin' WHERE SKIN_PATH = 'site.admin.skin';
UPDATE amee.SKIN SET PATH = 'skin-admin' WHERE ID=8;
UPDATE amee.SITE_APP set SKIN_PATH = 'skin-admin' WHERE SKIN_PATH = 'skin.admin.skin';
UPDATE amee.SKIN SET PATH = 'client-import' WHERE ID=10;
UPDATE amee.SITE_APP set SKIN_PATH = 'client-default' WHERE SKIN_PATH = 'default.client.skin';
UPDATE amee.SKIN SET PATH = 'client-default' WHERE ID=11;
UPDATE amee.SITE_APP set SKIN_PATH = 'client-default' WHERE SKIN_PATH = 'default.client.skin';
UPDATE amee.SKIN SET PATH = 'auth' WHERE ID=12;

# Fix AMEE
ALTER TABLE amee.ITEM DROP COLUMN START_DATE;
ALTER TABLE amee.ITEM CHANGE VALID_FROM START_DATE DATETIME;
UPDATE amee.ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE amee.ITEM SET AMOUNT = AMOUNT_PER_MONTH WHERE TYPE='PI';
UPDATE amee.ITEM SET AMOUNT = null WHERE TYPE='DI';
ALTER TABLE amee.ITEM DROP COLUMN AMOUNT_PER_MONTH;

# AMEE Defs
INSERT INTO amee.UNIT_DEFINITION (CREATED, MODIFIED, UID, ENVIRONMENT_ID, NAME, DESCRIPTION, UNITS, INTERNAL_UNIT) VALUES (CURDATE(), CURDATE(), 'AAAAAAAAAAAA', 1, 'distance', 'distance', 'km,mi', 'km');
INSERT INTO amee.UNIT_DEFINITION (CREATED, MODIFIED, UID, ENVIRONMENT_ID, NAME, DESCRIPTION, UNITS, INTERNAL_UNIT) VALUES (CURDATE(), CURDATE(), 'AAAAAAAAAAAB', 1, 'time', 'time', 'none,hour,day,month,year', 'year');
UPDATE amee.ITEM_VALUE_DEFINITION SET NAME='Distance', PATH='distance' WHERE PATH='distanceKmPerMonth';
UPDATE amee.ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID=1, PER_UNIT_DEFINITION_ID=2 WHERE PATH = 'distance';
UPDATE amee.ITEM_VALUE_DEFINITION SET NAME='KWh', PATH='kWh', VALUE=0, UNIT_DEFINITION_ID=3, PER_UNIT_DEFINITION_ID=2 WHERE PATH='kWhPerMonth';

# DELETE FROM ITEM_VALUE WHERE ID IN (SELECT ID FROM ITEM WHERE TYPE = 'PI');
# DELETE FROM ITEM WHERE TYPE = 'PI';
# DELETE FROM PERMISSION WHERE ID IN (SELECT PERMISSION_ID FROM PROFILE);
# DELETE FROM PROFILE;
# OPTIMIZE TABLE ITEM;
# OPTIMIZE TABLE ITEM_VALUE;
# OPTIMIZE TABLE PROFILE;

# Version
ALTER TABLE amee.ACTION DROP COLUMN VERSION;
ALTER TABLE amee.APP DROP COLUMN VERSION;
ALTER TABLE amee.ENVIRONMENT DROP COLUMN VERSION;
ALTER TABLE amee.GROUPS DROP COLUMN VERSION;
ALTER TABLE amee.GROUP_USER DROP COLUMN VERSION;
ALTER TABLE amee.PERMISSION DROP COLUMN VERSION;
ALTER TABLE amee.ROLE DROP COLUMN VERSION;
ALTER TABLE amee.SITE DROP COLUMN VERSION;
ALTER TABLE amee.SCHEDULED_TASK DROP COLUMN VERSION;
ALTER TABLE amee.SITE_ALIAS DROP COLUMN VERSION;
ALTER TABLE amee.SITE_APP DROP COLUMN VERSION;
ALTER TABLE amee.TARGET DROP COLUMN VERSION;
ALTER TABLE amee.SKIN DROP COLUMN VERSION;

# Algorithm
ALTER TABLE amee.ALGORITHM MODIFY COLUMN ITEM_DEFINITION_ID BIGINT (20) NULL;
ALTER TABLE amee.ALGORITHM ADD COLUMN TYPE VARCHAR(3) NOT NULL;
ALTER TABLE amee.ALGORITHM ADD COLUMN ALGORITHM_CONTEXT_ID bigint(20) default NULL;
UPDATE amee.ALGORITHM set TYPE='AL';
INSERT INTO amee.TARGET VALUES ('67','F613C476EADD','Algorithm Contexts Resource','','/{environmentUid}/algorithmContexts','algorithmContextsResource',b'00000000',b'00000000',b'00000001', SYSDATE(),SYSDATE(),'4','0');
INSERT INTO amee.TARGET VALUES ('68','21F188A4937F','Algorithm Context Resource','','/{environmentUid}/algorithmContexts/{algorithmContentUid}','algorithmContextResource',b'00000000',b'00000000',b'00000001',SYSDATE(),SYSDATE(),'4','0');

# UNIT_DEFINITIONS (DEV)
delete from UNIT_DEFINITION where id=4;
delete from UNIT_DEFINITION where id=3;

update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 105;
update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 107;
update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 127;

# UNUSED COLS
ALTER TABLE amee.ITEM DROP COLUMN UNIT;
ALTER TABLE amee.ITEM DROP COLUMN PER_UNIT;

# CORRECT PATH AND NAME FOR KWH
update ITEM_VALUE_DEFINITION set PATH='kWh', NAME='kWh' where PATH='kW';

# API Version
-- ## TODO: table creation and new column in amee.USER

INSERT INTO API_VERSION
VALUES	('1',SYSDATE(),SYSDATE(),'655B1AD17733','1.0','1'),
 		('2',SYSDATE(),SYSDATE(),'4D2BAA6BB1BE','2.0','1'),
		('3',SYSDATE(),SYSDATE(),'665B1AD17733','1.0','2'),
 		('4',SYSDATE(),SYSDATE(),'4E2BAA6BB1BE','2.0','2'),
		('5',SYSDATE(),SYSDATE(),'675B1AD17733','1.0','3'),
 		('6',SYSDATE(),SYSDATE(),'4F2BAA6BB1BE','2.0','3');

UPDATE amee.USER set API_VERSION = 1 WHERE ENVIRONMENT_ID=1;
UPDATE amee.USER set API_VERSION = 3 WHERE ENVIRONMENT_ID=2;
UPDATE amee.USER set API_VERSION = 5 WHERE ENVIRONMENT_ID=3;

# Remove redundant tables
DROP TABLE CLASS_ALIAS;