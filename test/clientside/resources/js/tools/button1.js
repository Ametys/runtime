/*
 *  Copyright 2015 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

var control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", {
    'id':               "button1",
    'opentool-role':    "uitool-tool1",
    'label':            "Tool one",
    'description':      "Open tool number one",
    'icon-small' :      "resources/img/editpaste_16.gif",
    'icon-medium' :     "resources/img/editpaste_32.gif",
    'icon-large' :      "resources/img/editpaste_48.gif"
});
Ametys.ribbon.RibbonManager.registerElement(control);
