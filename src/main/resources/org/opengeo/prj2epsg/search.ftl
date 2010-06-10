<#include "head.ftl">

      <p><big>Prj2EPSG is a simple service for converting <a href="http://en.wikipedia.org/wiki/Well-known_text#Spatial_reference_systems">well-known text</a> projection information from <a href="http://en.wikipedia.org/wiki/Shapefile#Shapefile_projection_format_.28.prj.29">.prj files</a> into standard <a href="http://en.wikipedia.org/wiki/European_Petroleum_Survey_Group">EPSG codes</a>.</big></p>

      <form class="input" action="search" method="post" enctype="multipart/form-data">
        <div class="options"><label for="terms">Paste a <acronym title="Well-known Text">WKT</acronym> definition or type keywords below</label>:
		</div>
		<p><textarea id="terms" name="terms" cols="60" rows="10">${html_terms}</textarea></p>
		<p>or <label for="prjfile">upload a .prj file</label>: <input type="file" id="prjfile" name="prjfile"/></p>
		<p><input type="submit" value="Convert" /></p>
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
