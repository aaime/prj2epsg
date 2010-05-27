<#include "head.ftl">

      <p><big>Prj2EPSG is a simple service for converting projection information from ESRI&apos;s&nbsp;Shapefile format into standard EPSG definitions.</big></p>
      <p>Paste a WKT definition from your .prj file or some keywords and press search:</p>

      <form class="input" method="get">
        <div class="options">Search for:
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
<#include "tail.ftl">