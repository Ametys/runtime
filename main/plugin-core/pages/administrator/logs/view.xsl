<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/Logger">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TITLE"/></title>                
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/logs.css" type="text/css"/>                  
            </head>
            
            <script>
            	<script type="text/javascript">
	               		function goBack()
	                    {
	                        document.location.href = context.workspaceContext;
	                    }   
	                   
	                    function view()
	                    {
	                        var elt = logview.getSelection()[0];
	                        if (elt.get('size') > 1024 * 1024)
	                        {
	                            if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW_CONFIRM"/>"))
	                            {
	                                download();
	                            }
	                        }
	                        else
	                        {
								window.location.href = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/view/" + encodeURIComponent(elt.get('location'));
	                        }
	                    }
	                    function download()
	                    {
	                        var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/download.zip";
	                        var args = "";
	                    
	                        var elts = logview.getSelection();
	                        for (var i = 0; i &lt; elts.length; i++)
	                        {
	                            var elt = elts[i];
	                            args += "file=" + encodeURIComponent(elt.get('location')) + "&amp;";
	                        }
	                        
	                        window.location.href = url + "?" + args;
                        }
                       

	                   function del()
	                    {
	                        if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_CONFIRM"/>"))
	                        {
								var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/delete";
	                            var args = "";
	                        
	                            var elts = logview.getSelection();
	                            for (var i = 0; i &lt; elts.length; i++)
	                            {
	                                var elt = elts[i];
	                                args += "file=" + encodeURIComponent(elt.get('location')) + "&amp;";
	                            }
	                            
	                            var result = Tools.postFromUrl(url, args);
	                            if (result == null)
	                            {
	                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR_GRAVE"/>")
	                                return;
	                            }
	                            
	                            var failuresString = Tools.getFromXML(result, "failure");
	                            
	                            for (var i = 0; i &lt; elts.length; i++)
	                            {
	                                var elt = elts[i];
	                                if (failuresString.indexOf('/' + elt.get('location') + '/') &lt; 0)
	                                {
	                                    logview.removeElement(elt);
	                                }
	                            }                            
	                            
	                            if (failuresString.length &gt; 0)
	                            {
	                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR"/>");
	                            }
	                        }
	                    }


	                    function purge()
	                    {
	                    	if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_CONFIRM"/>"))
	                        {
	                            var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/purge";
	
	                            var result = Tools.postFromUrl(url, "");
	                            if (result == null)
	                            {
	                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_ERROR_GRAVE"/>")
	                                return;
	                            }
	                            
	                            var doneString = Tools.getFromXML(result, "done");
	                            
	                            var nb = 0;
	                            var elts = logview.getElements();
	                            for (var i = elts.length - 1; i &gt;= 0; i--)
	                            {
	                                var elt = elts[i];
	                                if (doneString.indexOf('/' + elt.get('location') + '/') &gt;= 0)
	                                {
	                                    logview.removeElement(elt);
	                                    nb++;
	                                }
	                            }      
	                            
	                            alert(nb + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_DONE"/>")                      
	                        }
	                    }
	                    
	                    function fillSize(size)
	                    {
	                        while (size.length &lt; 20)
	                            size = "0" + size;
	                        return size;
	                    }
	                    
	                    function sizeRendered (size, metadata, record, rowIndex, colIndex, store)
	                    {
	                    	if (size &lt; 1024)
	                    	{
	                    		return size + " o";
	                    	}
	                    	else if (size &lt; 1024*1024)
	                    	{
	                    		return Math.round(size / 1024 * 10)/10 + " ko";
	                    	}
	                    	else if (size &lt; 1024*1024*1024)
	                    	{
	                    		return Math.round(size/1024/1024*10)/10 + " Mo";
	                    	}
	                    }
	                    
	                    function onSelectLog(grid, rowindex, e)
	                    {
							handle.showElt(0);
							handle.showElt(1);
							handle.showElt(2);
	                    }
	                    
	                    var logview;
	                    var handle;
	                    
						// Gestion
						handle = new Ext.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE"/>'});
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_16.gif", view);
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DOWNLOAD"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/zip_16.gif", download);
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/delete.gif", del);
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/purge.gif", purge);
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/quit.png", goBack);

						handle.hideElt(0);
						handle.hideElt(1);
						handle.hideElt(2);
						
						// Aide
						var help = new Ext.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>'});
						help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>");
						
						var rightPanel = new Ext.ametys.HtmlContainer({
									region:'east',
									border: false,
									baseCls: 'admin-right-panel',
									autoScroll: true,
									width: 277,
								    items: [handle, help]
						});
						
						var dummyDataLogs = [
							<xsl:for-each select="Logs/Log/file">
								['<xsl:value-of select="location" />',
								 '<xsl:value-of select="lastModified"/>',
								'<xsl:value-of select="size"/>',
                                 	'<xsl:choose>
                                    	<xsl:when test="../@name != location"><xsl:value-of select="../@name"/></xsl:when>
                                       <xsl:otherwise><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_GROUP_OTHERS"/></xsl:otherwise>
                                   </xsl:choose>']
								<xsl:if test="not(position()=last())">
									<xsl:text>,</xsl:text>
								</xsl:if>								
							</xsl:for-each>						
						              ];	
						              
					    var reader = new Ext.data.ArrayReader({}, [
					       {name: 'location'},
					       {name: 'date', type: 'date', dateFormat: 'Y-m-dTH:i'},
					       {name: 'size', type: 'int'},
					       {name: 'file'}
					    ]);
					    
					    var gpStore =  new Ext.data.GroupingStore({
					        reader: reader,
					        data : dummyDataLogs,
					        sortInfo:{field: 'location', direction: "ASC"},
					        groupField:'file'
					    });
						
						logview = new Ext.ametys.ListView({
							listeners: {'rowclick': onSelectLog},	
							animCollapse: true,					
						    store : gpStore,
							    view: new Ext.grid.GroupingView({
						            forceFit:true,
						            groupTextTpl: '{text}',
						            hideGroupedColumn : true
						        }),						    
						    columns: [
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_LABEL"/>", menuDisabled : true, sortable: true, dataIndex: 'file'},
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_NAME"/>", width : 250, menuDisabled : true, sortable: true, dataIndex: 'location'},
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_DATE"/>", width : 150, renderer: Ext.util.Format.dateRenderer('d F Y'), menuDisabled : true, sortable: true, dataIndex:'date',  align :'center'},
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_SIZE"/>", width : 100, renderer: sizeRendered, menuDisabled : true, sortable: true, dataIndex: 'size', align :'right'}
						    ],
								id: 'detailViewLogs',
								region: 'center',
								baseCls: 'detail-view-logs'
						});
						
						logview.setMultipleSelection(true);				
					
						function _getAdminPanel ()
						{
							return new Ext.Panel({
								region: 'center',
								baseCls: 'transparent-panel',
								border: false,
								layout: 'border',
								autoScroll: false,
								items: [logview, rightPanel]
							});
						}
						            			
            	</script>
            </script>
            <body>
				
            </body>
        </html>
    </xsl:template>
        
</xsl:stylesheet>
