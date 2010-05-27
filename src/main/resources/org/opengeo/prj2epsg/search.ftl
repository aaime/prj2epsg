<#include "head.ftl">

      <p><big>Prj2EPSG is a simple service for converting projection information from ESRI&apos;s&nbsp;Shapefile format into standard EPSG definitions.</big></p>
      <p>Paste a WKT definition from your .prj file or some keywords and press search:</p>

      <form class="input" method="get">
        <div class="options">Search for:
		</div>
		<textarea name="terms" cols="60" rows="10"">${html_terms}</textarea>
		<input type="submit" value="Search"/>
      </form>
   
      <#if html_showResults == true>
      <div>
        <#if (codes?size > 0)>
          <#if exact == true>
          <p>Found a single exact match for the specified search terms:
          <#else>
          <p>Found the following EPSG matches (sorted by relevance, ${codes?size} out of ${totalHits})</p>
          </#if>
          
          <ul>
             <#list codes as crs>
              <li><a href="./epsg/${crs.code}">${crs.code}</a> - ${crs.name}</li>
             </#list>
          </ul>
        <#else>
           <p>Could not find any match</p>
           <#if errors??>
           <p>${errors}</p>
           </#if>
        </#if>
      </div>
      </#if>
    
<#include "tail.ftl">