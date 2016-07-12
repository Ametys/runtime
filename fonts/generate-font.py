#  Copyright 2016 Anyware Services
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import fontforge
import argparse
import os
import fnmatch

parser = argparse.ArgumentParser(description='Generates fonts based on individual SVG glyphs.')
parser.add_argument('input', help='Directory containing SVG glyphs')
parser.add_argument('output', help='Destination directory')
parser.add_argument('fontName', help='Font name')
parser.add_argument('fontFamily', help='Font family')
parser.add_argument('cssPrefix', help='Css classes prefix')
args = parser.parse_args()

font = fontforge.font() # create new font
font.encoding = 'UnicodeFull' 
font.copyright = 'Ametys' 
font.fontname = 'Ametys'  
font.familyname = 'Ametys'  
font.fullname = 'Ametys'  
font.design_size = 16 
font.em = 512
font.descent = 64 
font.ascent = 512 - 64
font.autoWidth(0, 0, 512) 
font.is_quadratic = True

css = open(args.output + os.sep + args.fontName + '.css', 'w')

css.write("@font-face {\n")
css.write("    font-family: '" + args.fontFamily + "';\n")
css.write("    src: url('" + args.fontName + ".woff') format('woff');\n")
css.write("    font-weight: normal;\n")
css.write("    font-style: normal;\n")
css.write("}\n\n")
css.write('[class^="' + args.cssPrefix + '-"]:before, [class*=" ' + args.cssPrefix + '-"]:before,\n')
css.write('[class^="decorator-' + args.cssPrefix + '-"]:after, [class*=" decorator-' + args.cssPrefix + '-"]:after\n')
css.write('{\n')
css.write("   font-family: '" + args.fontFamily + "';\n")
css.write('   font-style: normal;\n')
css.write('   -webkit-font-smoothing: antialiased;\n')
css.write('   -moz-osx-font-smoothing: grayscale\n')
css.write('}\n\n')

html = open(args.output + os.sep + args.fontName + '.html', 'w')

html.write("<!DOCTYPE html>\n")
html.write("<html>\n")
html.write("<head>\n")
html.write("    <title>Ametys icons</title>\n")
html.write("    <link rel='stylesheet' type='text/css' href='" + args.fontName + ".css'>\n")
html.write("    <meta charset='UTF-8'>\n")
html.write("    <style>\n")
html.write("    body {\n")
html.write("        font-family: sans-serif;\n")
html.write("        line-height: 1.5;\n")
html.write("        font-size: 16px;\n")
html.write("        padding: 20px;\n")
html.write("        color:#333;\n")
html.write("    }\n")
html.write("    * {\n")
html.write("        -moz-box-sizing: border-box;\n")
html.write("        -webkit-box-sizing: border-box;\n")
html.write("        box-sizing: border-box;\n")
html.write("        margin: 0;\n")
html.write("        padding: 0;\n")
html.write("    }\n")
html.write("    #glyphs {\n")
html.write("        clear: both;\n")
html.write("        border-bottom: 1px solid #ccc;\n")
html.write("        padding: 2em 0;\n")
html.write("        text-align: center;\n")
html.write("    }\n")
html.write("    .glyph {\n")
html.write("        display: inline-block;\n")
html.write("        width: 9em;\n")
html.write("        margin: 1em;\n")
html.write("        text-align: center;\n")
html.write("        vertical-align: top;\n")
html.write("        background: #FFF;\n")
html.write("    }\n")
html.write("    .glyph .glyph-icon {\n")
html.write("        padding: 10px;\n")
html.write("        display: block;\n")
html.write("        font-size: 64px;\n")
html.write("        line-height: 1;\n")
html.write("    }\n")
html.write("    .glyph .glyph-icon:before {\n")
html.write("        font-size: 64px;\n")
html.write("        color: #222;\n")
html.write("        margin-left: 0;\n")
html.write("    }\n")
html.write("    .class-name {\n")
html.write("        font-size: 0.75em;\n")
html.write("        padding: 0.5em;\n")
html.write("        color: #666;\n")
html.write("        font-family: Courier New, monospace;\n")
html.write("    }\n")
html.write("    </style>\n")
html.write("</head>\n")
html.write("<body>\n")
html.write("    <section id='glyphs'>\n")


count = 0
for file in os.listdir(args.input):
    if fnmatch.fnmatch(file, '*.svg'):
        path = args.input + os.sep + file
        glyphName = file[:-4]
        code = 0xE000 + count
        strCode = hex(code)[2:]
        glyph = font.createChar(code, 'uni' + strCode.upper())
        count += 1
        glyph.importOutlines(path)
        glyph.left_side_bearing = glyph.right_side_bearing = 0
        css.write("." + args.cssPrefix + "-" + glyphName + ":before{content:'\\" + strCode + "';}\n")
        css.write(".decorator-" + args.cssPrefix + "-" + glyphName + ":after{content:'\\" + strCode + "';}\n")
        html.write("        <div class='glyph'>\n")
        html.write("            <div class='glyph-icon " + args.cssPrefix + "-" + glyphName + "'></div>\n")
        html.write("            <div class='class-name'>." + args.cssPrefix + "-" + glyphName + "</div>\n")
        html.write("        </div>\n")

css.close()

html.write("    </section>\n")
html.write("    </body>\n")
html.write("</html>\n")

html.close()

font.generate(args.output + os.sep + args.fontName + '.woff')

print '%i SVG glyphs imported in %s.woff' % (count, args.fontName)
