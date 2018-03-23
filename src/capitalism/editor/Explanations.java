/*
 * Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/
package capitalism.editor;

public class Explanations {

	public final static String industryText;
	public final static String commodityText;
	public final static String socialClassText;

	static {
		industryText = "Here, you build industries.\n\n"
				+ "To create a new industry, press the '+' button and you will be asked to fill in enough detail to make it. This minimal information "
				+ "is likely to be insufficient to ensure the industry will function properly; you can fill this out by editing the table above.\n\n "
				+ "Or, you can load a simulation that has already been defined, and either modify it or simply run it, to see what it does.\n\n"
				+ "An industry is any group of enterprises, or productive units, who make the same commodity using the same technology. "
				+ "More than one industry can produce the same commodity./n/n"
				+ "Industries use 'inputs' which are other commodities, used to make their product. Labour is an input to most"
				+ "industries. You can control which inputs an industry needs when you fill out its details.";
		commodityText = "Welcome to the capitalism App. \n\n" 
				+" This is the editor screen, which lets you create and modify projects. You can exit at any time by pressing the > button.\n\n"
				+ "To create a new commodity, press the '+' button. When you have created your new commodity, you can edit its details "
				+ "in the table above.\n\n"
				+ "Or, you can load a simulation that has already been defined, and either modify it or simply run it, to see what it does.\n\n"
				+ "A commodity is anything that is bought and sold. Some theories adopt stricter definitions; "
				+ "this App has space for you to provide your own definition.\n\n"
				+ "It distinguishes four types of commodities. A commodity is either destined to be consumed, in which case its 'function' "
				+ "is a 'Consumer Good', or to be used to produce other commodities, in which case its function is a 'Productive Input'.\n\n"
				+ "A commodity can either be produced by an industry, in which case its 'origin' is 'Industrial', or it can be "
				+ "produced by a social class, in which case its origin is 'social'. A commodity may not be produced at all, for example some types of money.";
		socialClassText = "To create a new social class, press the '+' button. This minimal information "
				+ "is likely to be insufficient to ensure the social class will function properly; you can complete the additional details "
				+ "by editing the social class in the table above, once you have created it. Or, you can load a simulation that has "
				+ "already been defined, and either modify it or simply run it, to see what it does.\n\n"
				+ "A social class is a group of people with a similar type of income or 'revenue', for example workers whose revenue comes from wages, or "
				+ "capitalists whose revenue derives from profit. You can specify where a class's revenue comes from \n\n"
				+ "Social Classes own, and consume, consumer goods, according to rules which you can control.";
	}
}
