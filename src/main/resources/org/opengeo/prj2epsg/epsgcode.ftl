<#include "head.ftl">

<h2>${code}</h2>

<ul>
<li><strong>Name</strong>: ${name}</li>
<li><strong>Scope</strong>: ${scope}</li>
<li><strong>Remarks</strong>: ${remarks}</li>
<li><strong>Area of validity</strong>: ${area}</li>
</ul>

<strong>WKT representation:</strong>
<textarea readonly cols="60" rows="10">
${wkt}
</textarea>

<#include "tail.ftl">
