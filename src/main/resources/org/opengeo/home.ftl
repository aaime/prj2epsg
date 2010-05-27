<#include "head.ftl">

<h1>Prj2EPSG</h1>
<p>Paste a WKT definition of the coordinate reference system you're looking for and press search:</p>
<form method="get">
  <textarea name="search" cols=120 rows=20>${search}</textarea><br/>
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