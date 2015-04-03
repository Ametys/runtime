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
 
/** Message target factories */
var mtFactory;
mtFactory = Ext.create("Ametys.message.factory.DefaultMessageTargetFactory", {type: "*", pluginName: "test", id: null});
Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);

mtFactory = Ext.create("Ametys.tool.ToolMessageTargetFactory", {type: "tool", pluginName: "test"});
Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);

function targetsToString(targets, offset)
{
    offset = offset || 0;
    
    var s = "";
    for (var i = 0; i < targets.length; i++)
    {
        var target = targets[i];
        
        if (offset != 0 && i != 0)
        {
            s += "\n";
        }
        
        for (var j = 0; j < offset; j++)
        {
            s += "    ";
        }
        
        s += "*" + target.getType() + "*\n"
        s += Ext.JSON.prettyEncode(target.getParameters(), offset);
        
        if (target.getSubtargets().length > 0)
        {
            s += "<br/>";
        }
        s += targetsToString(target.getSubtargets(), offset + 1);
    }
    
    if (targets.length == 0 && offset == 0)
    {
        s = "*No target*";
    }
    
    return s;
}
function spacepad(txt, size)
{
    txt = txt.substring(0, size);
    
    var sps = "";
    for (var i = 0; i < size - txt.length && size - txt.length > 0; i++)
    {
        sps += " ";
    }
    
    return txt + sps;
}
function spy(message)
{
    console.info("[Message] " + spacepad(message.getType(), 20) + " - " + targetsToString(message.getTargets()));
}
Ametys.message.MessageBus.on("*", spy, null)