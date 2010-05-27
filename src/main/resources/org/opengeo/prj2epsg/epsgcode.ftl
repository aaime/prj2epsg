<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>Prj2EPSG</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
    <link type="text/css" rel="stylesheet" href="../static/css/screen.css">
  </head>
<body>

<#setting number_format="#0.0#">

<h1>${code}</h1>

<ul>
<li><b>Name</b>: ${name}</li>
<li><b>Scope</b>: ${scope}</li>
<li><b>Remarks</b>: ${remarks}</li>
<li><b>Area of validity</b>: ${area}</li>
</ul>

<p>WKT representation:</p>
<pre>
${wkt}
</pre>

<p align="right">Prj2EPSG, a service provided by <a href="http://www.opengeo.org">OpenGeo</a></p>
</body>
</html>