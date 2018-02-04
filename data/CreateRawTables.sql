 DROP table if exists globals;
 CREATE TABLE `globals` ( `project` int default 1 not null, `timeStamp` VARCHAR (10) DEFAULT '1' not null, `RateOfExploitation` double DEFAULT NULL, `MELT` double DEFAULT NULL, initialCapital double DEFAULT NULL, currentCapital double DEFAULT NULL, profit double DEFAULT NULL, ProfitRate double DEFAULT NULL, `PopulationGrowthRate` double DEFAULT NULL, totalValue double DEFAULT 0, totalPrice double DEFAULT 0, investmentRatio double DEFAULT 0.0, labourSupplyResponse ENUM('FLEXIBLE','FIXED') DEFAULT 'FIXED', CurrencySymbol VARCHAR(10) DEFAULT '$', quantitySymbol VARCHAR(10) DEFAULT '#', Primary Key (project, timeStamp) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists socialClasses;
 CREATE TABLE `socialclasses` ( `project` INT DEFAULT 1 NOT NULL, `timeStamp` VARCHAR (10) DEFAULT '1' NOT NULL, `SocialClassName` VARCHAR(45) DEFAULT NULL, `Size` DOUBLE DEFAULT NULL, consumptionPerPerson double DEFAULT 0, reproductionTime double DEFAULT 1, revenue double DEFAULT 0, primary key (project, timeStamp, SocialClassName)) ENGINE=INNODB DEFAULT CHARSET=UTF8;
 
 DROP table if exists circuits;
 CREATE TABLE `circuits` ( `project` int default 1 not null, timeStamp VARCHAR (10) DEFAULT '1' not null, `productUseValueName` Varchar(45)not null, constrainedOutput double DEFAULT NULL, proposedOutput double DEFAULT NULL, costOfMPForExpansion double default 0, costOfLPforExpansion double default 0, GrowthRate double DEFAULT 0, InitialCapital double DEFAULT NULL, CurrentCapital double DEFAULT NULL, Profit double DEFAULT NULL, RateOfProfit double DEFAULT NULL, primary key (project, timeStamp, ProductUseValueName) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists stocks;
 CREATE TABLE `stocks` ( `project` int default 1 not null, `timeStamp` VARCHAR (10) DEFAULT '1' not null, OWNER varchar(45) not NULL, OWNERTYPE ENUM('CLASS','CIRCUIT') DEFAULT NULL, `usevalue` varchar(45) not NULL, `stockType` varchar(45) DEFAULT NULL, `quantity` double DEFAULT NULL, value double DEFAULT null, PRICE double DEFAULT NULL, `coefficient` double DEFAULT NULL, quantityDemanded  double DEFAULT NULL, primary key (project, timeStamp, owner, usevalue, stocktype) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists useValues;
 CREATE TABLE `useValues` ( `project` int default 1 not null, `timeStamp` VARCHAR (10) DEFAULT '1' not null, useValueName varchar(45) not NULL,`useValueCircuitType` ENUM('SOCIAL','CAPITALIST','MONEY') DEFAULT NULL, `unitValue` double DEFAULT NULL, `unitPrice` double DEFAULT NULL, `turnoverTime` double DEFAULT NULL, totalSupply double default NULL, totalQuantity double default NULL, totalDemand double default NULL,  surplusProduct double DEFAULT 0, totalValue double default NULL, totalPrice double default NULL, allocationShare double default null, useValueType ENUM('LABOURPOWER','MONEY','PRODUCTIVE','NECESSITIES','LUXURIES')DEFAULT 'UNKNOWN', capital double default 0, surplusValue double default 0, primary key (project, timeStamp, useValueName) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 DROP table if exists projects;
 CREATE TABLE `projects` ( `ProjectID` INT NOT NULL, `Description` VARCHAR(45) NULL, priceDynamics ENUM('SIMPLE','EQUALISE','DYNAMIC') DEFAULT 'SIMPLE',currentTimeStamp INT DEFAULT 1, currentTimeStampCursor INT DEFAULT 1, currentTimeStampComparatorCursor INT DEFAULT 1, ButtonState VARCHAR(20) DEFAULT NULL, PRIMARY KEY (`ProjectID`));
 
 DROP table if exists timeStamps;
 CREATE TABLE timeStamps (`timeStampID` int Default 1 NOT NULL, `projectFK` INT default 1 NOT NULL, period INT DEFAULT NULL,superState VARCHAR(45) default NULL, COMPARATORTIMESTAMPID INT DEFAULT 1, `Description` VARCHAR(30) default NULL, PRIMARY KEY (`timeStampID`,projectFK));
 
 insert into globals (project,timeStamp,RateOfExploitation,Melt,Profit,ProfitRate,PopulationGrowthRate,totalValue,totalPrice, investmentRatio,labourSupplyResponse,CurrencySymbol,quantitySymbol) select project,timeStamp,RateOfExploitation,Melt,Profit, ProfitRate,PopulationGrowthRate,totalValue,totalPrice,investmentRatio,labourSupplyResponse,CurrencySymbol,quantitySymbol from CSVREAD('~/Documents/Capsim/data/globals.csv');

 insert into socialClasses select * from CSVREAD('~/Documents/Capsim/data/socialClasses.csv');	
 
 insert into circuits (PROJECT, TIMESTAMP, PRODUCTUSEVALUENAME, CONSTRAINEDOUTPUT, PROPOSEDOUTPUT, GROWTHRATE) select PROJECT, TIMESTAMP, PRODUCTUSEVALUENAME, CONSTRAINEDOUTPUT, PROPOSEDOUTPUT, GROWTHRATE  from CSVREAD('~/Documents/Capsim/data/circuits.csv');
 
 insert into useValues select * from CSVREAD('~/Documents/Capsim/data/useValues.csv');

 INSERT INTO timestamps (timeStampID, projectFK, PERIOD,superState, COMPARATORTIMESTAMPID, Description) SELECT TIMESTAMPID, PROJECTFK, PERIOD, SUPERSTATE, COMPARATORTIMESTAMPID, DESCRIPTION FROM CSVREAD('~/Documents/Capsim/data/timeStamps.csv');
 
 insert into projects (ProjectID, Description,priceDynamics) select ProjectID, Description,priceDynamics  from CSVREAD('~/Documents/Capsim/data/projects.csv');

 update projects set currentTimeStamp =1 where ProjectID=1;
 
 update projects set currentTimeStampCursor =1 where ProjectID=1;

 -- not all columns are filled for every type of stock: for example, sales and money stocks have no coefficient
 -- we keep them all in the same table, because for certain operations (for example calculating total value) we need to iterate over all stocks
 -- and because it greatly simplifies the code.
 -- possibly, in a future version, separate tables could be introduced for each type of stock, with proper functional abstraction to deal with the variety among them.
 
 insert into stocks (PROJECT, TIMESTAMP, OWNER, OWNERTYPE, USEVALUE, STOCKTYPE, QUANTITY, COEFFICIENT, QUANTITYDEMANDED) select PROJECT, TIMESTAMP, OWNER, OWNERTYPE, USEVALUE, STOCKTYPE, QUANTITY, COEFFICIENT, QUANTITYDEMANDED from CSVREAD('~/Documents/Capsim/data/stocks.csv');
 