/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
 
function myFunction()
{
  alert("Not internationalized message");
  
  var test = true;
  for (var i = 0; i < 5 && test; i++)
  {
    test = i == 3;
  }
}