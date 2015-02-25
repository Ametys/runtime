/*
 *  Copyright 2014 Anyware Services
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
/**
 * @inheritdoc Ext.MessageBox
 * Redefine Ext.MessageBox to use a new instance of {@link Ext.window.MessageBox} each time it is called.
 */
Ametys.MessageBox = Ametys.Msg  = {
	
	/**
	  * @inheritdoc Ext.Msg#buttonText
	  */
		
	/**
	 * @inheritdoc Ext.Msg#show
	 */
	show: function(cfg)
	{
		var mb = new Ext.window.MessageBox();
		mb.on('hide', function () {Ext.destroy(this)});
		return mb['show'].apply(mb, arguments);
	},
	
	/**
	 * @inheritdoc Ext.Msg#alert
	 */
	alert: function (msg, fn, scope)
	{
		var mb = new Ext.window.MessageBox();
		mb.on('hide', function () {Ext.destroy(this)});
		return mb['alert'].apply(mb, arguments);
	},
	
	/**
	 * @inheritdoc Ext.Msg#progress
	 */
	progress: function(cfg, msg, progressText)
	{
		var mb = new Ext.window.MessageBox();
		mb.on('hide', function () {Ext.destroy(this)});
		return mb['progress'].apply(mb, arguments);
    },
    
    /**
	 * @inheritdoc Ext.Msg#wait
	 */
    wait : function(cfg, title, config)
    {
    	var mb = new Ext.window.MessageBox();
    	mb.on('hide', function () {Ext.destroy(this)});
    	return mb['wait'].apply(mb, arguments);
    },
    
    /**
	 * @inheritdoc Ext.Msg#prompt
	 */
    prompt : function(cfg, msg, fn, scope, multiline, value)
    {
    	var mb = new Ext.window.MessageBox();
    	mb.on('hide', function () {Ext.destroy(this)});
    	return mb['prompt'].apply(mb, arguments);
    },
    
    /**
	 * @inheritdoc Ext.Msg#confirm
	 */
    confirm: function(cfg, msg, fn, scope)
    {
    	var mb = new Ext.window.MessageBox();
    	mb.on('hide', function () {Ext.destroy(this)});
    	return mb['confirm'].apply(mb, arguments);
    }
}