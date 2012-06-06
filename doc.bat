@echo off
cd D:\Dev\raphael\Ametys\kernel\01_RUNTIME\LTE

jsduck-3.10.3.exe main\kernel\resources\js main\workspace-admin\resources\js D:\Libraries\Javascript\ext-4.1.0\src --builtin-classes --images D:\Libraries\Javascript\ext-4.1.0\docs\images --output doc --ignore-global --title='Ametys' --footer='Javascript documentation for Ametys'

pause