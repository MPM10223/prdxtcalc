@REM unzip JRE
cscript /B /Nologo ..\util\unzip.vbs ..\JRE\jre6.zip "%ROLEROOT%\approot"
 
@REM start the server
cd "%ROLEROOT%\approot\bin"
start "%ROLEROOT%\approot\jre6\bin\java.exe JobRunner hwvhpv4cb1.database.windows.net 1433 prdxt jobQueue prdxt_calc@hwvhpv4cb1 s1mul@t3"