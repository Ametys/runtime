var monindex = 1;

function openTool1()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°1 - ' + monindex++,
        description: 'This is my tool numero uno',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "je suis un test Tool 1",
        closable: true
    });

    layout.addTool(tool, "cl");
    layout.focusTool(tool);
}

function openTool2()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°2 - ' + monindex++,
        description: 'This is my tool numero duo',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 2</b>"
    });
    
    layout.addTool(tool, "l");
    layout.focusTool(tool);
}

function openTool3()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°3 - ' + monindex++,
        description: 'This is my tool numero trouo',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 3</b>"
    });
    
    layout.addTool(tool, "cr");
    layout.focusTool(tool);
}


function openTool4()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°4 - ' + monindex++,
        description: 'This is my tool numero quatro',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 4</b>"
    });
    
    layout.addTool(tool, "r");
    layout.focusTool(tool);
}



function openTool5()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°5 - ' + monindex++,
        description: 'This is my tool numero cinquo',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 5</b>"
    });
    
    layout.addTool(tool, "t");
    layout.focusTool(tool);
}



function openTool6()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°6 - ' + monindex++,
        description: 'This is my tool numero sexto',
        smallIcon: '/resources/img/editpaste_16.gif',
        mediumIcon: '/resources/img/editpaste_32.gif',
        largeIcon: '/resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 6</b>"
    });
    
    layout.addTool(tool, "b");
    layout.focusTool(tool);
}
