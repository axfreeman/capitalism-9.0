# Capitalism 9.0 announcements

## 2/5/2018 9:19:41 AM 

Thorough overhaul of underlying database storage and retrieval.
Result is a much more robust and simpler project (see 'todo.md' for tech details)
All projects working except SR with profit rate equalization; however currently this is only a simple consistency check since profit rates are already equal for SR project 1
Before proceeding to deal exhaustively with profit rate equalization, it was necessary first to ensure that the project could accomodate multiple industries and multiple different types of means of production. The underlying code is now there, but has to be tested.

## 2/4/2018 2/4/2018 5:49:20 PM 

New option 'deltas' allows user to display the differences between changed items and their predecessors
Working for all items in the main tables.
Hence new executable.
In this version, the project 'SR with halved period' is not working. 
Since it was working not long ago, I'll delay fixing this until equal-profit-rate is fully working, or at least, tested on a more general example.
The current test simply checks to see it produces the same results as no equalization but is a bit trivial since SR has equal profit rates in any case.
Some work is needed to introduce a more thoroughgoing test, for example, allowing for two means of production in the accumulation phase.

Data Management overhaul (necessary to introduce the above generalizations) is almost complete, but has uncovered a bug in H2. 
Slightly techie explanation: this occurs when an 'enum' type is part of the primary key of a table.
It prevents me from removing, from the code, anything that depends on the names that the user has given to individual industries and classes
And thereby prevents us, for example, describing 'Means of Production' as a type of product, of which there can be many instances.

## 2/4/2018 10:40:30 AM 

Corrected a number of faults in data management, as a result of which SR with equalization is close to working.
Provisional updated to the code has been committed, but no new executable or Jar, in case there are errors as yet undetected

## 2/3/2018 10:16:39 PM 

Corrected silly graphics bug.
New Executable

## 2/3/2018 8:28:27 PM 

The grouped columns in the UseValue table can be contracted and expanded by double-clicking
New Executable

## 2/2/2018 3:55:22 PM 

Graphic display now fully working

## 1/31/2018 5:47:04 PM 

Code for price dynamics added, dealing with profit rate equalization.
Not fully working yet; hence no new executable

## 1/31/2018 9:52:57 AM 

Important corrections to the MELT calculation and to 'Consequences' action.
In preparation for transformation of values into prices of production.

## 1/30/2018 12:33:45 PM 

Currency symbols added to the tables display.
New executable

## 1/29/2018 3:38:27 PM 

Project 5 (Expanded Reproduction) is now working.
Watershed.
New Executable ('capitalism.exe')
User Guide will now be updated to correspond to the new version.

## 1/28/2018 1:38:04 PM 

Projects 1,2,3 now working.
There is a new executable ('capitalism.exe')

## 1/28/2018 9:49:58 AM

Capitalism 9.0 starts where a previous version, capitalism-8.0, left off. The older version is a working prototype that reached a watershed; it contains a lot of development code which is not especially interesting. However, the old version is archived if users want to study the genesis and history of the app.

It contains five 'projects' which (in principle) serve the following purposes:

1. They  comprise a set of tests to make sure the app works.
2. They provide worked examples so the user can learn the app.
3. They illustrate important principles in economics.

The transition to capitalism-9.0 took place when some new features were being incorporated. At the start point, only project 1 was working.

	Projects 2 and 3 go slightly wrong (though a working version can be found in the capitalism-8.0 archive); I aim to restore these to working condition before proceeding with project 5
	Project 4 is a full-blown simulation that isn't working yet in this version (it worked in 1992)
	Project 5 is undergoing testing at this time.
