# TODO 3/11/2018 9:59:59 AM 

Editor well under way.
Load from current project should be automatic not optional
Need to distinguish actual stock level from required stock level.
Should save to a new project which should then be output as an XML file
Main simulation then check that we can load this
Code to build a new project from scratch
Code to add commodities, add industries, add social classes
Intercept if the editor window is closed and data has not been saved
Overhaul the simulation so as to do away with coefficients, which are confusing. Instead, introduce the variable 'required stock level'
Need to calculate, and display, the rate of exploitation. It follows from the wage.
Proper shutdown.
Debts and loans; a capital account.
Multiple consumption goods
Dockable windows

###VALIDATION (USER DATA)

Use values given in 'stocks' must exist
A Use value called 'consumption' must exist (Better: configure the name of the consumption good/s in the configuration file)
A Use value called 'Labour Power' must exist 
Every circuit's name must be some useValue. Not every usevalue needs to be produced(Money) but if it is consumed it must be produced (& vice versa? No: eg collectors' items)
Every Productive Stock must belong to a circuit and consist of a usevalue
Every Industry must have exactly one stock of every possible input
Every Sales Stock must belong to a circuit or a social class and consist of a usevalue
Every Money Stock must belong to a circuit or a social class and consist of a usevalue
Every social class must have a sales stock for labour power even if it doesn't have any to sell (!)
(The two-sector project inadvertently declared a usevalue called fixed for which there was no circuit: should be detected)
User must supply a timeStamp.csv; every project must have a timestamp and it must be 1.

every project that is referenced by a timeStamp record should exist in the projects table
there should be a record for every timeStamp that is referenced by any other table in the database
every timeStamp that is referenced by a project record should exist in the timestamp table
there should be a record for every project record that is referenced by any other table in the database
If a commodity type is productive, we should disallow stocks of it being consumption goods, and vice versa
Every class should have a sales stock of labour power even if it doesn't produce it.
Because during the simulation, it can start producing labour power if the participationRatio changes

###HEALTH CHECK (DURING THE SIMULATION)

The following invariants should be preserved:

1. the value of a money stock, measured in labour time, following the TSS interpretation of Marx, is given by the magnitude of the money divided by the MELT, the Monetary Expression of Labour Time. 
2. In production, the value transferred to the product by consumed inputs is given, similarly, by the value represented by their money price.
3. total value can only change through production (which includes reproduction and hence consumption by classes as well as in production); neither trade nor distribution should alter the global total
2. the total new value in production should equal the labour power consumed

4. the total profit should be equal to the total new value produced, less the value consumed by workers
5. in trade, the total price of the stocks owned by each circuit and each class is constant (provided of course money is included), but the value is not. (Value is thus appropriated, or redistributed, by owners as a result of selling goods whose value is lower or higher, in relative terms, than their price.)

Further invariants to be listed following the document 'Mathematical Foundations of the Value Theory of Finance GitHub version.doc' in the docs folder


