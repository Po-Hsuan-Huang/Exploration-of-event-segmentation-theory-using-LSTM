/*******************************************************************************
 * JANNLab Neural Network Framework for Java
 * Copyright (C) 2012-2013 Sebastian Otte
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jannlab.generator.exception;


/**
 * No more modification allowed during modeling.
 * <br></br>
 * @author Sebastian Otte
 */
public class NoModificationAllowed extends NetGeneratorException {
    private static final long serialVersionUID = -678831812096197825L;

   public NoModificationAllowed() { super("no modification allowed."); } 
}
