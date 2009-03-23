/*
Use in <HEAD> with DEFER keyword wrapped in conditional comments:
<!--[if lt IE 7]>
<script defer type="text/javascript" src="pngfix.js"></script>
<![endif]-->
*/

var pngFix = {

    transparentGifLocation: context.contextPath + "/_admin/resources/img/transparent.gif",
    
    _getBackGround: function(el) {
        var bg = null;
        var bgRepeat = null;
        var styleSheet = null;
        var rule = null;
        for (var s=0; s < document.styleSheets.length; s++) {
            for (var r=0; r < document.styleSheets[s].rules.length; r++) {
                var sel = document.styleSheets[s].rules[r].selectorText;
                var c = sel.split(".");
                if(c.length == 2 && c[0].toUpperCase() == el.nodeName.toUpperCase() && c[1] == el.className) {
                    if(document.styleSheets[s].rules[r].style.backgroundImage) {
                        bg = document.styleSheets[s].rules[r].style.backgroundImage;
                        styleSheet = document.styleSheets[s];
                        rule = document.styleSheets[s].rules[r];
                    }
                    bgRepeat = document.styleSheets[s].rules[r].style.backgroundRepeat || bgRepeat;
                }
                c = sel.split("#");
                if(c.length == 2 && c[0].toUpperCase() == el.nodeName.toUpperCase() && c[1] == el.id) {
                    if(document.styleSheets[s].rules[r].style.backgroundImage) {
                        bg = document.styleSheets[s].rules[r].style.backgroundImage;
                        styleSheet = document.styleSheets[s];
                        rule = document.styleSheets[s].rules[r];
                    }
                    bgRepeat = document.styleSheets[s].rules[r].style.backgroundRepeat || bgRepeat;
                }
            }
        }
        return {styleSheet: styleSheet, rule: rule, bg: bg, bgRepeat: bgRepeat};
    },
    
    _computePath: function(src, styleSheet) {
        var splitSrc = src.split("../");
        if(splitSrc.length > 1) {
            var split = "";
            if(styleSheet) {
                split = styleSheet.href.split("/");
            } else {
                split = location.href.split("/");
            }
            var result = "";
            for(var i=0; i < split.length - splitSrc.length; i++) {
                result += split[i] + "/";
            }
            result += splitSrc[splitSrc.length - 1];
            return result;
        } else {
            return src;
        }
    },
    
    fixAllByTagName: function(tags) {
        for(var i=0; i<tags.length; i++) {
            var elements = document.getElementsByTagName(tags[i]);
            for(var j=0; j<elements.length; j++){
                this.fixElement(elements[j]);
            }
        }
    },
    
    fixElement: function(el, force) {
        if ((parseFloat(navigator.appVersion.split("MSIE")[1]) >= 5.5) && (document.body.filters)) {
            if(el.nodeName.toUpperCase() == "IMG") {
                var src = el.src;
                if(src && (force || src.toUpperCase().substring(src.length-3, src.length) == "PNG")) {
                	var sizingMethod = "image";
                	for(var i=0; i<el.attributes.length; i++) {
                		if(el.attributes[i].name == "sizingMethod") {
                			sizingMethod = el.attributes[i].value;
                		}
                	}
                    el.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled='true', src='" + src + "', sizingMethod='" + sizingMethod + "')";
                    el.oldSrc = el.src;
                    el.src = this.transparentGifLocation;
                }   
            } else {
                var src = el.style.backgroundImage;
                if(src && (force || src.toUpperCase().substring(src.length-4, src.length-1) == "PNG")) {
                    var path = this._computePath(src.substring(4, src.length-1));
                    var repeat = el.style.backgroundRepeat;
                    var sizingMethod = "crop";
                    if(repeat && repeat.indexOf("repeat") == 0) {
                        sizingMethod = "scale";
                    }
                    el.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled='true', src='" + path + "', sizingMethod='" + sizingMethod + "')";
                    el.style.backgroundImage = "url('" + this.transparentGifLocation + "')";
                    el.oldSrc = src;
                } else {
                    src = this._getBackGround(el);
                    if(src.rule && src.bg && src.bg.toUpperCase().substring(src.bg.length-4, src.bg.length-1) == "PNG") {
                        var path = this._computePath(src.bg.substring(4, src.bg.length-1), src.styleSheet);
                        var sizingMethod = "crop";
                        if(src.bgRepeat && src.bgRepeat.indexOf("repeat") == 0) {
                            sizingMethod = "scale";
                        }
                        src.rule.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled='true', src='" + path + "', sizingMethod='" + sizingMethod + "')";
                        src.rule.style.backgroundImage = "url('" + this.transparentGifLocation + "')";
                        el.oldSrc = src;
                    }               
                }
            }
        }
    }
}

pngFix.fixAllByTagName(["img", "div", "td"]);