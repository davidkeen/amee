# Remove redundant tables
DROP TABLE amee.CLASS_ALIAS;
DROP TABLE amee.SKIN_IMPORT;
DROP TABLE amee.SKIN;

# Remove version columns
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

# Fix Environments & Users
UPDATE amee.ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE amee.ENVIRONMENT SET OWNER = '';
UPDATE amee.USER SET LOCATION = '';
UPDATE amee.USER SET NICK_NAME = '';
UPDATE amee.USER SET STATUS = 0;
UPDATE amee.USER SET USER_TYPE = 0;
UPDATE amee.USER SET USER_TYPE = 3 WHERE USERNAME = 'root';
ALTER TABLE amee.USER MODIFY COLUMN PASSWORD VARCHAR(40);
ALTER TABLE amee.USER DROP COLUMN SUPER_USER;

# Fix Apps, Sites, etc.
UPDATE amee.APP SET TARGET_BUILDER = '';
UPDATE amee.SITE SET SERVER_ADDRESS = '', SERVER_PORT = '', SERVER_SCHEME = '', SECURE_AVAILABLE = 0;
DELETE FROM amee.TARGET WHERE TARGET='skinRenderResource';

# Fix Items
ALTER TABLE amee.ITEM DROP COLUMN START_DATE;
ALTER TABLE amee.ITEM CHANGE VALID_FROM START_DATE DATETIME;
UPDATE amee.ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE amee.ITEM SET AMOUNT = AMOUNT_PER_MONTH WHERE TYPE='PI';
UPDATE amee.ITEM SET AMOUNT = null WHERE TYPE='DI';
ALTER TABLE amee.ITEM DROP COLUMN AMOUNT_PER_MONTH;

# Fix Algorithms
ALTER TABLE amee.ALGORITHM MODIFY COLUMN ITEM_DEFINITION_ID BIGINT (20) NULL;
ALTER TABLE amee.ALGORITHM ADD COLUMN TYPE VARCHAR(3) NOT NULL;
ALTER TABLE amee.ALGORITHM ADD COLUMN ALGORITHM_CONTEXT_ID bigint(20) default NULL;
UPDATE amee.ALGORITHM set TYPE='AL';

# Add Algorithm targets
INSERT INTO amee.TARGET
(ID, UID, NAME, DESCRIPTION, URI_PATTERN, TARGET, DEFAULT_TARGET, DIRECTORY_TARGET, ENABLED, CREATED, MODIFIED, APP_ID, TYPE)
VALUES ('67', 'F613C476EADD', 'Algorithm Contexts Resource', '', '/{environmentUid}/algorithmContexts',
        'algorithmContextsResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0'),
       ('68', '21F188A4937F', 'Algorithm Context Resource', '', '/{environmentUid}/algorithmContexts/{algorithmContentUid}',
        'algorithmContextResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0');

# Add API Version
INSERT INTO amee.API_VERSION (ID, CREATED, MODIFIED, UID, VERSION)
VALUES	('1', SYSDATE(), SYSDATE(), '655B1AD17733', '1.0'),
 		('2', SYSDATE(), SYSDATE(), '4D2BAA6BB1BE', '2.0');

UPDATE amee.USER set API_VERSION = 1;


# Remove all profiles.
# DELETE FROM ITEM_VALUE WHERE ID IN (SELECT ID FROM ITEM WHERE TYPE = 'PI');
# DELETE FROM ITEM WHERE TYPE = 'PI';
# DELETE FROM PERMISSION WHERE ID IN (SELECT PERMISSION_ID FROM PROFILE);
# DELETE FROM PROFILE;
# OPTIMIZE TABLE ITEM;
# OPTIMIZE TABLE ITEM_VALUE;
# OPTIMIZE TABLE PROFILE;

# Everything below this point seems to be demo specific???

# Create/update Definitions
INSERT INTO amee.UNIT_DEFINITION (CREATED, MODIFIED, UID, ENVIRONMENT_ID, NAME, DESCRIPTION, UNITS, INTERNAL_UNIT) VALUES (CURDATE(), CURDATE(), 'AAAAAAAAAAAA', 1, 'distance', 'distance', 'km,mi', 'km');
INSERT INTO amee.UNIT_DEFINITION (CREATED, MODIFIED, UID, ENVIRONMENT_ID, NAME, DESCRIPTION, UNITS, INTERNAL_UNIT) VALUES (CURDATE(), CURDATE(), 'AAAAAAAAAAAB', 1, 'time', 'time', 'none,hour,day,month,year', 'year');
UPDATE amee.ITEM_VALUE_DEFINITION SET NAME='Distance', PATH='distance' WHERE PATH='distanceKmPerMonth';
UPDATE amee.ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID=1, PER_UNIT_DEFINITION_ID=2 WHERE PATH = 'distance';
UPDATE amee.ITEM_VALUE_DEFINITION SET NAME='KWh', PATH='kWh', VALUE=0, UNIT_DEFINITION_ID=3, PER_UNIT_DEFINITION_ID=2 WHERE PATH='kWhPerMonth';

# UNIT_DEFINITIONS (DEV)
delete from UNIT_DEFINITION where id=4;
delete from UNIT_DEFINITION where id=3;
update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 105;
update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 107;
update ITEM_VALUE_DEFINITION set UNIT_DEFINITION_ID = null where id = 127;

# UNUSED COLS (DEV)
ALTER TABLE amee.ITEM DROP COLUMN UNIT;
ALTER TABLE amee.ITEM DROP COLUMN PER_UNIT;

# CORRECT PATH AND NAME FOR KWH
update ITEM_VALUE_DEFINITION set PATH='kWh', NAME='kWh' where PATH='kW';

# Drop redundant VERSION column in USER table
ALTER TABLE `amee`.`USER` DROP COLUMN `VERSION`;
