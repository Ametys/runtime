Ext.define("Ext.theme.neptune.Component",{override:"Ext.Component",initComponent:function(){this.callParent();if(this.dock&&this.border===undefined){this.border=false}},privates:{initStyles:function(){var c=this,b=c.hasOwnProperty("border"),a=c.border;if(c.dock){c.border=null}c.callParent(arguments);if(b){c.border=a}else{delete c.border}}}},function(){Ext.namespace("Ext.theme.is").Neptune=true;Ext.theme.name="Neptune"});Ext.define("Ext.theme.neptune.resizer.Splitter",{override:"Ext.resizer.Splitter",size:8});Ext.define("Ext.theme.ametysbase.resizer.Splitter",{override:"Ext.resizer.Splitter",size:2});Ext.define("Ext.theme.neptune.toolbar.Toolbar",{override:"Ext.toolbar.Toolbar",usePlainButtons:false,border:false});Ext.define("Ext.theme.neptune.layout.component.Dock",{override:"Ext.layout.component.Dock",noBorderClassTable:[0,Ext.baseCSSPrefix+"noborder-l",Ext.baseCSSPrefix+"noborder-b",Ext.baseCSSPrefix+"noborder-bl",Ext.baseCSSPrefix+"noborder-r",Ext.baseCSSPrefix+"noborder-rl",Ext.baseCSSPrefix+"noborder-rb",Ext.baseCSSPrefix+"noborder-rbl",Ext.baseCSSPrefix+"noborder-t",Ext.baseCSSPrefix+"noborder-tl",Ext.baseCSSPrefix+"noborder-tb",Ext.baseCSSPrefix+"noborder-tbl",Ext.baseCSSPrefix+"noborder-tr",Ext.baseCSSPrefix+"noborder-trl",Ext.baseCSSPrefix+"noborder-trb",Ext.baseCSSPrefix+"noborder-trbl"],edgeMasks:{top:8,right:4,bottom:2,left:1},handleItemBorders:function(){var y=this,f=0,z=8,A=4,l=2,e=1,a=y.owner,s=a.bodyBorder,n=a.border,j=y.collapsed,p=y.edgeMasks,k=y.noBorderClassTable,x=a.dockedItems.generation,w,d,v,h,r,m,u,o,g,q,t,c;if(y.initializedBorders===x){return}t=[];c=[];d=y.getBorderCollapseTable();k=y.getBorderClassTable?y.getBorderClassTable():k;y.initializedBorders=x;y.collapsed=false;v=y.getDockedItems("visual");y.collapsed=j;for(r=0,m=v.length;r<m;r++){u=v[r];if(u.ignoreBorderManagement){continue}o=u.dock;q=h=0;t.length=0;c.length=0;if(o!=="bottom"){if(f&z){w=u.border}else{w=n;if(w!==false){h+=z}}if(w===false){q+=z}}if(o!=="left"){if(f&A){w=u.border}else{w=n;if(w!==false){h+=A}}if(w===false){q+=A}}if(o!=="top"){if(f&l){w=u.border}else{w=n;if(w!==false){h+=l}}if(w===false){q+=l}}if(o!=="right"){if(f&e){w=u.border}else{w=n;if(w!==false){h+=e}}if(w===false){q+=e}}if((g=u.lastBorderMask)!==q){u.lastBorderMask=q;if(g){c[0]=k[g]}if(q){t[0]=k[q]}}if((g=u.lastBorderCollapse)!==h){u.lastBorderCollapse=h;if(g){c[c.length]=d[g]}if(h){t[t.length]=d[h]}}if(c.length){u.removeCls(c)}if(t.length){u.addCls(t)}f|=p[o]}q=h=0;t.length=0;c.length=0;if(f&z){w=s}else{w=n;if(w!==false){h+=z}}if(w===false){q+=z}if(f&A){w=s}else{w=n;if(w!==false){h+=A}}if(w===false){q+=A}if(f&l){w=s}else{w=n;if(w!==false){h+=l}}if(w===false){q+=l}if(f&e){w=s}else{w=n;if(w!==false){h+=e}}if(w===false){q+=e}if((g=y.lastBodyBorderMask)!==q){y.lastBodyBorderMask=q;if(g){c[0]=k[g]}if(q){t[0]=k[q]}}if((g=y.lastBodyBorderCollapse)!==h){y.lastBodyBorderCollapse=h;if(g){c[c.length]=d[g]}if(h){t[t.length]=d[h]}}if(c.length){a.removeBodyCls(c)}if(t.length){a.addBodyCls(t)}},onRemove:function(d){var c=this,b=d.lastBorderMask,a=d.lastBorderCollapse;if(!d.destroyed&&!d.ignoreBorderManagement){if(b){d.lastBorderMask=0;d.removeCls(c.noBorderClassTable[b])}if(a){d.lastBorderCollapse=0;d.removeCls(c.getBorderCollapseTable()[a])}}c.callParent([d])}});Ext.define("Ext.theme.neptune.panel.Panel",{override:"Ext.panel.Panel",border:false,bodyBorder:false,initBorderProps:Ext.emptyFn,initBodyBorder:function(){if(this.bodyBorder!==true){this.callParent()}}});Ext.define("Ext.theme.neptune.container.ButtonGroup",{override:"Ext.container.ButtonGroup",usePlainButtons:false});Ext.define("Ext.theme.neptune.toolbar.Paging",{override:"Ext.toolbar.Paging",defaultButtonUI:"plain-toolbar",inputItemWidth:40});Ext.define("Ext.theme.neptune.picker.Month",{override:"Ext.picker.Month",measureMaxHeight:36});Ext.define("Ext.theme.neptune.form.field.HtmlEditor",{override:"Ext.form.field.HtmlEditor",defaultButtonUI:"plain-toolbar"});Ext.define("Ext.theme.neptune.panel.Table",{override:"Ext.panel.Table",lockableBodyBorder:true,initComponent:function(){var a=this;a.callParent();if(!a.hasOwnProperty("bodyBorder")&&!a.hideHeaders&&(a.lockableBodyBorder||!a.lockable)){a.bodyBorder=true}}});Ext.define("Ext.theme.neptune.grid.RowEditor",{override:"Ext.grid.RowEditor",buttonUI:"default-toolbar"});Ext.define("Ext.theme.neptune.grid.column.RowNumberer",{override:"Ext.grid.column.RowNumberer",width:25});Ext.define("Ext.theme.neptune.menu.Separator",{override:"Ext.menu.Separator",border:true});Ext.define("Ext.theme.neptune.menu.Menu",{override:"Ext.menu.Menu",showSeparator:false});Ext.define("Ext.theme.ametysbase.tree.Panel",{override:"Ext.tree.Panel",lines:false});Ext.define("Ametys.theme.ametysbase.grid.plugin.Multisort",{override:"Ametys.grid.plugin.Multisort",closeItemButtonWidth:12});Ext.define("Ametys.theme.ametysbase.ui.fluent.ribbon.GroupScale",{override:"Ametys.ui.fluent.ribbon.GroupScale",headerPosition:"bottom",titleAlign:"center",border:true});Ext.define("Ametys.theme.ametysbase.ui.fluent.ribbon.Ribbon.ContextualTabGroup",{override:"Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroup",margin:"0 0 0 1"});Ext.define("Ametys.theme.ametysbase.ui.fluent.ribbon.TabPanel",{override:"Ametys.ui.fluent.ribbon.TabPanel",userCfg:{arrowVisible:false}});(function(){function c(d){d.setMaxWidth(70+Ext.create("Ext.util.TextMetrics",d.btnInnerEl).getWidth(d.text))}function a(d){d.setMaxHeight(42+Ext.create("Ext.util.TextMetrics",d.btnInnerEl).getHeight("a")*2)}function b(d){d._rawText=d.getText();d.getText=function(){return this._rawText};d.setText=function(l){var n=this._rawText;this._rawText=l;this.text=l;var h=Ext.create("Ext.util.TextMetrics",this.btnInnerEl);var e=this.text;var f=h.getWidth(e);var g="";var j=this.text.indexOf(" ",0);while(j!=-1&&j<this.text.length){var k=this.text.substring(j+1);var i=this.text.substring(0,j)+"<br/>"+k;var m=h.getWidth(i);if(m<f){f=m;e=i;g=k}j=this.text.indexOf(" ",j+1)}this.text=e;this.btnInnerEl.setHtml(this.text);this.fireEvent("textchange",this,n,this._rawText)};d.setText(d.getText())}Ext.define("Ametys.theme.ametysbase.ui.tool.layout.ZonedTabsToolsLayout",{override:"Ametys.ui.tool.layout.ZonedTabsToolsLayout",statics:{__ADDITIONNAL_ZONE_CONFIG_OTHER:{tabBar:{defaults:{flex:1,minWidth:56,textAlign:"left",listeners:{afterrender:c,textchange:c}}}},__ADDITIONNAL_ZONE_CONFIG_LEFT:{headerPosition:"left",tabPosition:"left",tabRotation:0,tabBar:{defaults:{flex:1,minHeight:56,iconAlign:"top",textAlign:"center",listeners:{afterrender:b,textchange:a}}}},__ADDITIONNAL_ZONE_CONFIG_RIGHT:{headerPosition:"right",tabPosition:"right",tabRotation:0,tabBar:{defaults:{flex:1,minHeight:56,iconAlign:"top",textAlign:"center",listeners:{afterrender:b,textchange:a}}}}}})})();Ext.namespace("Ext.theme.is")["ametys-base"]=true;Ext.theme.name="ametys-base";
