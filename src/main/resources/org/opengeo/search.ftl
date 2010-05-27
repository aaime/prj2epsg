<#include "head.ftl">

<h1>Prj2EPSG</h1>
<p>Paste a WKT definition of the coordinate reference system you're looking for, or some keyworkds, and press search:</p>
<form method="get">
  Search mode: 
  <select name="mode">
    <option value="wkt" selected="${selection.wkt}">WKT</option>
    <option value="keywords" selected="${selection.keywords}">Keywords</option>
  </select><br/>
  <textarea name="terms" cols=120 rows=20>${terms}</textarea><br/>
  <input type="submit" value="Search"/>
</form>

<#if showResults == true>
  <p>${message}</p>
  <#if (codes?size > 0)>
    <ul>
      <#list codes as code>
        <li><a href="./epsg/${code}">${code}</a></li>
      </#list>
    </ul>
  </#if>
</#if>

<#include "tail.ftl">