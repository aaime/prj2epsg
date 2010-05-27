<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>Prj2EPSG</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
    <link type="text/css" rel="stylesheet" href="static/css/screen.css">
  </head>
<body>

<#setting number_format="#0.0#">

  <div class="container">
    <div class="header">
      <h1>Prj2EPSG</h1>
      <p><a class="logo" href="http://www.opengeo.org">OpenGeo</a></p>
    </div><!-- End .header -->

    <div class="content">
      <p><big>Prj2EPSG is a simple service for converting projection information from ESRI&apos;s&nbsp;Shapefile format into standard EPSG definitions.</big></p>
      <p>Paste a WKT definition from your .prj file or some keywords and press search:</p>

      <form class="input" method="get">
        <div class="options">Search mode:
          <select name="mode">
	  	    <option value="wkt" ${selection.wkt}>WKT</option>
		    <option value="keywords" ${selection.keywords}>Keywords</option>
          </select> 
		</div>
		<textarea name="terms" cols="60" rows="10"">${terms}</textarea>
		<input type="submit" value="Search"/>
      </form>
   

      <#if showResults == true>
      <p>${message}</p>
        <#if (codes?size > 0)>
        <ul>
           <#list codes as crs>
            <li><a href="./epsg/${crs.code}">${crs.code}</a> - ${crs.name}</li>
           </#list>
        </ul>
        </#if>
      </#if>
    </div>
    <div class="footer">
      <h2>About</h2>
        <p>This service was built by <a href="http://www.opengeo.org">OpenGeo</a> using Geotools, Restle, Freemarker, and Lucene and is running version X.x of the ESPG database. Hosting is generously provided by <a href="http://www.skygoneinc.com/">SkyGone</a>.</p>
        <h2>Documentation</h2>
        <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.</p>
        <h3>API Documentation</h3>
        <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.</p>
    </div><!-- End .footer -->
  </div> <!-- End .content -->
</body>
</html>