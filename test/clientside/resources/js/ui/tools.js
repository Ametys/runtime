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
        html: 'This is tool one<br/>'
            + '<ul>'
            +       '<li>Contextual tab 1 <a href="javascript:tabContextual1.showContextualTab()">show</a> <a href="javascript:tabContextual1.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2 <a href="javascript:tabContextual2.showContextualTab()">show</a> <a href="javascript:tabContextual2.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2bis <a href="javascript:tabContextual2bis.showContextualTab()">show</a> <a href="javascript:tabContextual2bis.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 3 <a href="javascript:tabContextual3.showContextualTab()">show</a> <a href="javascript:tabContextual3.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 4 <a href="javascript:tabContextual4.showContextualTab()">show</a> <a href="javascript:tabContextual4.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 5 <a href="javascript:tabContextual5.showContextualTab()">show</a> <a href="javascript:tabContextual5.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 6 <a href="javascript:tabContextual6.showContextualTab()">show</a> <a href="javascript:tabContextual6.hideContextualTab()">hide</a></li>'
            + '</ul>',
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
