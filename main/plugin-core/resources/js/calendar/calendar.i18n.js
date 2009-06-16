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
// ** I18N

// Calendar EN language
// Author: Mihai Bazon, &lt;mihai_bazon@yahoo.com&gt;
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// Translator: David Duret, &lt;pilgrim@mala-template.net&gt; from previous french version

// full day names
Calendar._DN = new Array
("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_1"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_2"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_3"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_4"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_5"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_6"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DAY_0"/>");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_1"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_2"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_3"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_4"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_5"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_6"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SDAY_0"/>");

// full month names
Calendar._MN = new Array
("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_1"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_2"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_3"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_4"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_5"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_6"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_7"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_8"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_9"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_10"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MONTH_11"/>");
 
  Calendar._FD = parseInt("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_FIRSTDAYINWEEK"/>");
  if (isNaN(Calendar._FD))
      Calendar._FD = 0;

// short month names
Calendar._SMN = new Array
("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>",
 "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SMONTH_0"/>");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "";

Calendar._TT["ABOUT"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_HELP_ABOUT"/>";
Calendar._TT["ABOUT_TIME"] = "\n\n<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_HELP_ABOUTTIME"/>";

Calendar._TT["PREV_YEAR"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_YEAR_PREV"/>";
Calendar._TT["PREV_MONTH"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_MONTH_PREV"/>";
Calendar._TT["GO_TODAY"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_GOTODAY"/>";
Calendar._TT["NEXT_MONTH"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_MONTH_NEXT"/>";
Calendar._TT["NEXT_YEAR"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_YEAR_NEXT"/>";
Calendar._TT["SEL_DATE"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_SETDATE"/>";
Calendar._TT["DRAG_TO_MOVE"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_MOVE"/>";
Calendar._TT["PART_TODAY"] = " (<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_TODAY"/>)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_DAYFIRST"/>";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_WEEKEND"/>";

Calendar._TT["CLOSE"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_CLOSE"/>";
Calendar._TT["TODAY"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_TODAY"/>";
Calendar._TT["TIME_PART"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_TIME"/>";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_SAVEDATEFORMAT"/>";
Calendar._TT["TT_DATE_FORMAT"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_DATEFORMAT"/>";

Calendar._TT["WK"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_WEEK_LABEL"/>";
Calendar._TT["TIME"] = "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_TIP_TIME_LABEL"/>";
