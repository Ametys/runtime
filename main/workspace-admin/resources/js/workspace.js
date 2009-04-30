function workspaceBody () {}
 
function onreadyfunction() 
{
	var TOP_HEIGHT = 90;
	var FOOTER_HEIGHT = 35;
	var PADDLE_WIDTH = 44;
	var MAIN_WIDTH = 1018;
	var CONTENT_WIDTH = 930;
	
	var contentLeft = new Ext.ametys.HtmlContainer( {
		region :'west',
		width :PADDLE_WIDTH,
		baseCls :'',
		id :'content_left'
	});
	
	var contentRight = new Ext.ametys.HtmlContainer( {
		region :'east',
		width :PADDLE_WIDTH,
		baseCls :'',
		id :'content_right'
	});
	
	/** Bandeau */
	var top = new Ext.ametys.HtmlContainer(
			{
				region :'north',
				id :'top',
				height :TOP_HEIGHT,
				baseCls: '',
				contentEl :'logo'
			});
	
	/** Contenu principal */
	var main = new Ext.ametys.HtmlContainer( {
		region :'center',
		id :'main',
		layout: 'fit',
		autoScroll: false,
		baseCls: '',
		items : [ workspaceBody() ]
	});
	
	/** Pied de page (versions) */
	var footer = new Ext.ametys.HtmlContainer( {
		region :'south',
		height: FOOTER_HEIGHT,
		id :'footer',
		baseCls: '',
		contentEl : 'versions'
	});
	
	var contentCenter = new Ext.ametys.HtmlContainer( {
		region :'center',
		id :'content_center',
		width: CONTENT_WIDTH,
		layout :'border',
		items : [top, main, footer]
	});

	var mainContent = new Ext.ametys.HtmlContainer( {
		layout :'border',
		id :'wrapper',
		width: MAIN_WIDTH,
		baseCls :'',
		region :'center',
		items : [ contentLeft, contentCenter, contentRight ]
	})

	var leftColumn = new Ext.ametys.HtmlContainer( {
		id :'column-left',
		baseCls :'',
		region :'west'
	});
	var rightColumn = new Ext.ametys.HtmlContainer( {
		id :'column-right',
		baseCls :'',
		region :'east'
	});

	var mainLayout = new Ext.Viewport( {
		layout :'border',
		items : [ leftColumn, mainContent, rightColumn ],
		listeners : {
			'resize' : function(vp, adjWidth, adjHeight, rawWidth, rawHeight) {
				// Calcul de la largeur des colonnes
				// de droite et de gauche en
				// fonction de la taille de la
				// fenetre du navigateur
				var width = adjWidth;
				if (width == null || width == 'auto') {
					width = this.getSize()['width'];
				}
				var columnWidth = (width - MAIN_WIDTH) / 2;
				leftColumn.setWidth(columnWidth);
				rightColumn.setWidth(columnWidth);
			}
		}
	});

	Ext.QuickTips.init();
}

Ext.onReady(onreadyfunction);

function pngFixFn() 
{
	if (window.pngFix)
	{
		pngFix.fixAllByTagName(["img", "div"]);
	}
}
Ext.onReady(pngFixFn);

