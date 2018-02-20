 DROP table if exists globals;
 CREATE TABLE globals ( project int default 1 not null, timeStamp VARCHAR (10) DEFAULT '1' not null, RateOfExploitation double DEFAULT NULL, 
 MELT double DEFAULT NULL, initialCapital double DEFAULT NULL, currentCapital double DEFAULT NULL, profit double DEFAULT NULL, ProfitRate double DEFAULT NULL, 
 PopulationGrowthRate double DEFAULT NULL, totalValue double DEFAULT 0, totalPrice double DEFAULT 0, investmentRatio double DEFAULT 0.0, 
 labourSupplyResponse ENUM('FLEXIBLE','FIXED') DEFAULT 'FIXED', CurrencySymbol VARCHAR(10) DEFAULT '£', quantitySymbol VARCHAR(10) DEFAULT '#', 
 Primary Key (project, timeStamp) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists socialClasses;
 CREATE TABLE socialclasses ( project INT DEFAULT 1 NOT NULL, timeStamp VARCHAR (10) DEFAULT '1' NOT NULL, SocialClassName VARCHAR(45) DEFAULT NULL, 
 Size DOUBLE DEFAULT NULL, participationRatio double DEFAULT 1, revenue double DEFAULT 0, primary key (project, timeStamp, SocialClassName)) ENGINE=INNODB DEFAULT CHARSET=UTF8;
 
 DROP table if exists industries;
 CREATE TABLE industries ( project int default 1 not null, timeStamp VARCHAR (10) DEFAULT '1' not null, industryName Varchar(45)not null, 
 commodityName VARCHAR(45) default null,output double DEFAULT NULL, proposedOutput double DEFAULT NULL, GrowthRate double DEFAULT 0, InitialCapital double DEFAULT NULL, 
 primary key (project, timeStamp, industryName) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists stocks;
 CREATE TABLE stocks ( project int default 1 not null, timeStamp VARCHAR (10) DEFAULT '1' not null, OWNER varchar(45) not NULL, OWNERTYPE ENUM('CLASS','INDUSTRY') DEFAULT NULL, 
 commodity varchar(45) not NULL, stockType varchar(45) DEFAULT NULL, quantity double DEFAULT 0, value double DEFAULT 0, PRICE double DEFAULT 0, 
 productionCoefficient double DEFAULT 0, consumptionCoefficient double DEFAULT 0, 
 replenishmentDemand double DEFAULT 0, expansionDemand double DEFAULT 0, primary key (project, timeStamp, owner, commodity, stocktype) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists commodities;
 CREATE TABLE commodities ( project int default 1 not null, timeStamp VARCHAR (10) DEFAULT '1' not null, name varchar(45) not NULL,
 originType ENUM('SOCIALLY_PRODUCED','INDUSTRIALLY_PRODUCED','MONEY') DEFAULT NULL, unitValue double DEFAULT NULL, unitPrice double DEFAULT NULL, 
 turnoverTime double DEFAULT NULL, surplusProduct double DEFAULT 0, allocationShare double default null, 
 functionType ENUM('MONEY','PRODUCTIVE_INPUT','CONSUMER_GOOD') DEFAULT null, stockUsedUp double default 0, 
 stockProduced double default 0, imageName VARCHAR(45) default null, toolTip VARCHAR (255) default null, displayOrder INT DEFAULT 0, 
 primary key (project, timeStamp, name) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 CREATE INDEX IDX_TO_DISPLAYORDER ON COMMODITIES(displayOrder); 
 
 DROP table if exists projects;
 CREATE TABLE projects ( ProjectID INT NOT NULL, Description VARCHAR(45) NULL, priceDynamics ENUM('SIMPLE','EQUALISE','DYNAMIC') DEFAULT 'SIMPLE',
 currentTimeStamp INT DEFAULT 1, currentTimeStampCursor INT DEFAULT 1, currentTimeStampComparatorCursor INT DEFAULT 1,
 period INT DEFAULT 1, ButtonState VARCHAR(20) DEFAULT NULL, PRIMARY KEY (ProjectID));
 
 DROP table if exists timeStamps;
 CREATE TABLE timeStamps (timeStampID int Default 1 NOT NULL, projectFK INT default 1 NOT NULL, period INT DEFAULT NULL,superState VARCHAR(45) default NULL, 
 COMPARATORTIMESTAMPID INT DEFAULT 1, Description VARCHAR(30) default NULL, PRIMARY KEY (timeStampID,projectFK));
 
 insert into globals (project,timeStamp,Melt,PopulationGrowthRate,investmentRatio,labourSupplyResponse, CurrencySymbol,quantitySymbol) 
 select project,timeStamp,Melt,PopulationGrowthRate,investmentRatio,labourSupplyResponse,CurrencySymbol,quantitySymbol 
 from CSVREAD('~/Documents/Capsim/data/globals.csv');

 insert into socialClasses select * from CSVREAD('~/Documents/Capsim/data/socialClasses.csv');	
 
 insert into industries (PROJECT, TIMESTAMP, INDUSTRYNAME, COMMODITYNAME, OUTPUT, GROWTHRATE) select PROJECT, TIMESTAMP, 
 INDUSTRYNAME, COMMODITYNAME, OUTPUT, GROWTHRATE from CSVREAD('~/Documents/Capsim/data/industries.csv');
 
 insert into commodities (PROJECT, TIMESTAMP, NAME,functionType,UNITVALUE,UNITPRICE,TURNOVERTIME,originType,IMAGENAME,DISPLAYORDER,TOOLTIP) 
 select PROJECT, TIMESTAMP, NAME,functionType,UNITVALUE,UNITPRICE,TURNOVERTIME,originType,IMAGENAME,DISPLAYORDER, TOOLTIP from CSVREAD('~/Documents/Capsim/data/commodities.csv');

 INSERT INTO timestamps (timeStampID, projectFK, PERIOD,superState, COMPARATORTIMESTAMPID, Description) 
 SELECT TIMESTAMPID, PROJECTFK, PERIOD, SUPERSTATE, COMPARATORTIMESTAMPID, DESCRIPTION FROM CSVREAD('~/Documents/Capsim/data/timeStamps.csv');
 
 insert into projects (ProjectID, Description,priceDynamics) select ProjectID, Description,priceDynamics from CSVREAD('~/Documents/Capsim/data/projects.csv');

 update projects set currentTimeStamp =1 where ProjectID=1;
 
 update projects set currentTimeStampCursor =1 where ProjectID=1;

 -- not all columns are filled for every type of stock: for example, sales and money stocks have no coefficient
 -- we keep them all in the same table, because for certain operations (for example calculating total value) we need to iterate over all stocks
 -- and because it greatly simplifies the code.
 -- possibly, in a future version, separate tables could be introduced for each type of stock, with proper functional abstraction to deal with the variety among them.
 
 insert into stocks (PROJECT, TIMESTAMP, OWNER, OWNERTYPE, COMMODITY, STOCKTYPE, QUANTITY, PRODUCTIONCOEFFICIENT, CONSUMPTIONCOEFFICIENT) 
 select PROJECT, TIMESTAMP, OWNER, OWNERTYPE, COMMODITY, STOCKTYPE, QUANTITY, PRODUCTIONCOEFFICIENT,CONSUMPTIONCOEFFICIENT from CSVREAD('~/Documents/Capsim/data/stocks.csv');
 