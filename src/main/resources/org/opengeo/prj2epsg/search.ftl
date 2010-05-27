<#include "head.ftl">

      <p><big>Prj2EPSG is a simple service for converting projection information from ESRI&apos;s <a href="http://en.wikipedia.org/wiki/Shapefile#Shapefile_projection_format_.28.prj.29">Shapefile projection format</a> into standard <a href="http://en.wikipedia.org/wiki/European_Petroleum_Survey_Group">EPSG codes</a>.</big></p>

      <form class="input" action="search" method="POST" enctype="multipart/form-data">
        <div class="options"><label for="terms">Paste a <acronym title="Well-known Text">WKT</acronym> definition or type keywords below</label>:
		</div>
		<textarea id="terms" name="terms" cols="60" rows="10"">${html_terms}</textarea>
		<p>or <label for="prjfile">upload a .prj file</label>: <input type="file" id="prjfile" name="prjfile"/></p>
		<input type="submit" value="Convert"/>
      </form>
   
      <#if html_showResults == true>
      <div id="results">
        <h2>Results</h2>
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
           <p>Could not find any matches.</p>
           <#if errors??>
           <p>${errors}</p>
           </#if>
        </#if>
      </div>
      </#if>
    
<#include "tail.ftl">