# TODO 2/11/2018 6:41:20 AM  

Major overhaul of visuals and database, to accommodate multiple consumption goods, is complete
However now need to work through the various projects, all of which except 1 are manifesting quirks.

## MINOR

Need to calculate, and display, the rate of exploitation. It follows from the wage.
Proper shutdown.

## MAJOR

Debts and loans; a capital account.
Multiple consumption goods
Dockable windows

## BUGS

All projects except 1 have bugs or quirks

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

###HEALTH CHECK (DURING THE SIMULATION)

The following invariants should be preserved:

1. the value of a money stock, measured in labour time, following the TSS interpretation of Marx, is given by the magnitude of the money divided by the MELT, the Monetary Expression of Labour Time. 
2. In production, the value transferred to the product by consumed inputs is given, similarly, by the value represented by their money price.
3. total value can only change through production (which includes reproduction and hence consumption by classes as well as in production); neither trade nor distribution should alter the global total
2. the total new value in production should equal the labour power consumed

4. the total profit should be equal to the total new value produced, less the value consumed by workers
5. in trade, the total price of the stocks owned by each circuit and each class is constant (provided of course money is included), but the value is not. (Value is thus appropriated, or redistributed, by owners as a result of selling goods whose value is lower or higher, in relative terms, than their price.)

Further invariants to be listed following the document 'Mathematical Foundations of the Value Theory of Finance GitHub version.doc' in the docs folder

## FIXED
FIXED 2/11/2018 6:42:50 AM Tree carries ghost of previous simulations after restart
FIXED 2/11/2018 6:42:33 AM Globals Differencing
FIXED 2/3/2018 8:27:11 PM Hints have gone wobbly
GONE 2/2/2018 3:46:00 PM double-clicking a column to resize it provokes a fetch of a null table row
FIXED 2/2/2018 3:44:35 PM graphics switching finally sorted, plus consistent loading of tables without redrawing 
FIXED (Partly) 1/29/2018 3:43:58 PM Configure the name of the consumption good and perhaps workers/capitalists/labour power
FIXED 1/29/2018 3:44:19 PM log window is now scrollable
FIXED 1/29/2018 3:43:19 PM TreeView: when adding to period 2, previous periods should be unexpanded
FIXED (more or less) 1/29/2018 3:42:45 PM License, copyright and acknowledgements display check it's in all classes
FIXED 1/29/2018 3:42:17 PM Coefficients are showing in red in the stocks table
FIXED 3:41:25 PM Complete the Distribution phase
FIXED 1/2/2018 11:48:42 AM Fix the treeview Startup bug CANCEL - still not working 1/9/2018 4:18:58 PM 
FIXED 1/1/2018 6:42:56 PM Action Buttons and TreeView were not working properly with switch projects. Is now OK
FIXED 1/1/2018 6:43:30 PM Project selection combo box now functioning more clearly
DROPPED 12/31/2017 3:18:56 PM SelectionsProvider perhaps should not be a separate class
DROPPED 12/31/2017 3:19:19 PM Change the names of the named queries to be more generic (ie there is no need for the name of the class to be included- it's obvious from the context)
FIXED 12/31/2017 3:17:59 PM Update the user guide
DONE 12/31/2017 3:18:09 PM Separate out 3 examples of Simple Reproduction (but with bugs see compliance list
FIXED 12/31/2017 3:18:09 PM Rounding and precision.
FIXED some time before 12/31/2017 11:37:07 AM graphics not visible
FIXED 12/31/2017 11:34:21 AM Differencing: if it's a superstate, comparison should be with the last superstate. Tricky.
FIXED 12/31/2017 11:30:49 AM Example with price<>value to show the effect of the expression buttons.
UPDATED 12/31/2017 11:34:53 AM Handling of intrinsic/extrinsic value and recalculation of the MELT
FIXED 12/31/2017 11:31:02 AM Clearer separation of the buttons in the top  bar
FIXED 12/31/2017 11:31:13 AM Hours to money button
FIXED 12/30/2017 6:37:03 PM Initial profit rate should be null
DROPPED 12/30/2017 6:37:19 PM Initial MELT should be calculated?
FIXED Treelist for timeStamp selection
FIXED Differencing in the dynamic circuits table
GONE 12/30/2017 6:15:21 PM Choose between NaN and 'number'. No real call for it; also the underlying issue is to have a proper plurality of value concepts
FIXED 12/30/2017 6:08:43 PM Sales stocks: have an 'owner type' and distinguish using colour and sorting
FIXED 12/30/2017 4:17:31 PM M-C does not show supply and demand as having changed
FIXED 12/29/2017 5:37:21 PM Disable 'one period'
FIXED 12/29/2017 4:53:13 PM Workers have $1500 more than they need.
FIXED 12/29/2017 4:45:40 PM Move circuits 1 above circuits 2 in the main tables.
FIXED 12/29/2017 4:45:49 PM DON'T show 'productive capital'. It only confuses things.
FIXED 12/29/2017 4:33:27 PM The sales stocks don't say what the use is
FIXED 12/29/2017 4:33:52 PM  Stock tables right adjust
FIXED 12/29/2017 2:30:32 PM 'Exchange/Production/Distribution'(modified by the graphics button).
FIXED 12/29/2017 2:29:24 PM Increase the height of the money column
FIXED 12/29/2017 2:28:53 PM Rename 'circuit' to 'branch' or perhaps 'industry'
FIXED 12/29/2017 2:26:46 PM Rename UV table to be 'commodities' table
FIXED 12/29/2017 2:27:18 PM UV table rename 'commodity name' and 'producer' and put owner second
FIXED 12/29/2017 2:27:18 PM Display  the stock tables as money and consumption goods first, then sales, finally production
FIXED 12/23/2017 11:05:07 AM Finish off the display including consistent differencing
FIXED 12/19/2017 12:04:03 PM Dynamic Circuit Table reconstructed so that it can display differences
FIXED 12/18/2017 7:09:22 PM  thousands separator for large numbers
FIXED 12/17/2017 12:45:18 PM There was no bug in Distribution; money adjusted so it is exactly enough for reproduction
FIXED 12/17/2017 12:45:18 PM Styling the button treeview - can probably do without. Return to this when creating a treeview for the timestamp display
FIXED 12/17/2017 12:44:53 PM Complete the TreeView for timeStamps 
FIXED 12/17/2017 12:44:53 PM consider exactly where the timeStamp entity is modified; drive it from the ActionStates rather than leaving it to the individual commands
FIXED 12/12/2017 9:24:14 PM Bug has appeared because of Tree Buttons: crashes when switching projects
FIXED 12/13/2017 11:12:41 AM New bug noted: graphics are not visible when capitalism.exe is run from a different location (this is a getresource() problem)
FIXED 12/12/2017 5:49:36 PM Button list for ActionButtons
FIXED 12/10/2017 5:08:39 AM graphics display more or less complete.
GONE 12/9/2017 12:06:05 PM There doesn't seem to be anything wrong with RefreshDisplay; it just makes a lot of calls on the CellFactories. Will monitor this.
FIXED 12/9/2017 12:06:15 PM Dump files to user directory and load from user directory. But very few validity checks
FIXED 12/8/2017 8:53:07 AM (after considerable work: more difficult than expected) Make the executable location-independent and test on other machines and platforms
PARTIAL 12/4/2017 7:00:05 PM Log files and database now located in the user directory
FINISHED 12/4/2017 4:33:00 AM Major restructure of Table Viewer code, to simplify usage by making explicit use of TableCell subclasses.  
GONE 12/4/2017 4:32:11 AM Quirky cellValueFactory visit to Bankers twice during each refresh
STARTED 12/3/2017 8:47:05 PM Major restructure of Table Viewer code, to simplify usage by making explicit use of TableCell subclasses.  
FIXED 12/2/2017 10:34:09 PM Revamp the Globals record in line with the other persistent entities
FIXED 12/2/2017 10:34:25 PM Finish the graphics display
FIXED 12/2/2017 3:29:40 PM Custom controls for display of the Globals
FIXED 11/27/2017 2:38:39 PM Finish log window hierarchical display
FIXED 11/27/2017 2:37:25 PM Check copy statements for changes we made to the variables in the persistent entities
FIXED 11/27/2017 7:39:51 AM Tooltip for the tables
FIXED 11/27/2017 6:50:28 AM TreeView-style log
FIXED 11/26/2017 7:49:18 PM Custom logging appender to show rolling log
FIXED 11/26/2017 7:48:47 PM Squash logger code to make the code more readable
FIXED 11/25/2017 4:56:38 PM 'Soft' user messages when a programme error occurred
DROPPED 11/25/2017 8:53:56 AM Column widths somewhat unpredictable, following introduction of TabbedTableViewer. In fact this is basically OK now. May revisit
DROPPED 11/25/2017 8:54:41 AM For the capitalist class it should be derived from the profit/investment process.
FIXED 11/25/2017 8:53:06 AM New global 'capitalist consumption' or something like that. In fact involved an overhaul of consumption. Logically quite complex. See comments in 'RegisterDemand'
FIXED 11/23/2017 5:56:36 PM The buttons called 'quantity',  'value' and 'price' are intended to control the dynamic circuit table display. As yet, they don't...
DROPPED 11/23/2017 5:56:07 PM Consumption demand for workers should be determined by the rate of exploitation (is now done by the value of the wage)
FIXED 11/23/2017 5:54:46 PM Enum for the button labels
FIXED 11/23/2017 5:54:54 PM more Tooltips
FIXED 11/23/2017 5:55:02 PM Display the total price and total value of the social classes and the circuits, in the appropriate table
DROPPED 11/22/2017 12:01:00 PM SimulationManager and ViewManager should make no calls that explicitly retrieve JPA entities.
FIXED 11/22/2017 11:53:14 AM Make the managers static
FIXED 11/22/2017 11:52:35 AM Study the commit() strategy. Perhaps we don't need to commit anything until 'MoveOneStep' since the entities are all made persistent and therefore staged for committing.
FIXED  11/21/2017 1:35:10 PM Table display should be a custom control
FIXED 11/21/2017 1:35:10 PM Consistent use of utility function 'changeBy' and 'setTo' for stocks 
FIXED Calculate the supply of labour power from the size of the workforce
FIXED Null pointer bug
FIXED non-differencing stock bug on social classes.
FIXED dynamic table still to go in
FIXED some tooltips
FIXED 11/11/2017 3:54:32 PM Radio buttons to choose the timeStampCursor
FIXED Not enough workers! Labour Power in short supply.
FIXED 11/19/2017 11:59:15 AM consumption goods reduce correctly during social reproduction
FIXED 11/19/2017 11:59:28 AM on Check that after a restart (or a start) nothing changes when the timeStamp is selected
FIXED 11/19/2017 Unit price, for simple reproduction, rises to 2.17. This was because of the consumption bug
FIXED Why do workers lose their consumption goods in Trade? This should happen in Produce 
FIXED Stocks table for social classes is empty
FIXED Why do we initialize twice from the standard queries?
