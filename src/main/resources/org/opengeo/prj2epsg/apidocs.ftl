<#include "head.ftl">
       
       <h2>Searching for EPSG codes</h2>
       <p>Programmers interested in using prj2EPSG as a web service can perform a GET request against <code>${html_webroot}/search.&lt;type&gt;</code> with the following parameters:</p>
       <ul>
         <li><strong>terms</strong>: the search terms, which can be a set of keywords, or a full WKT representation (e.g., the contents of a prj file)</li>
         <li><strong>mode</strong>: possible values are <strong>auto</strong>, <strong>wkt</strong> and <strong>keywords</strong>, with <strong>auto</strong> being the default if the parameter is not specified. In <strong>keywords</strong> mode the terms will be interpreted as a set of keywords to be searched for, in <strong>wkt</strong> mode the system will parse the representation and look for a match, in <strong>auto</strong> mode the server will automatically 
             figure out the appropriate mode by checking if terms is a valid WKT representation.</li>
       </ul>
       
       <p>If &lt;type&gt; is <strong>html</strong> the usual HTML response will be provided, if <strong>json</strong> is used the response will be a JSON document with the following fields:</p>
       <ul> 
         <li><strong>exact</strong>: true if the provided WKT could be matched exactly to one entry in the EPSG database, false otherwise</li>
         <li><strong>totalHits</strong>: total amount of potential results found in the database. The actual codes list is always capped to 20</li>
         <li><strong>error</strong>: reports WKT parsing errors, if any</li>
         <li><strong>codes</strong>: a list of EPSG code objects, each one containg:
           <ul>
             <li><strong>code</strong>: the EPSG code</li>
             <li><strong>name</strong>: the coordinate reference system name</li>
             <li><strong>url</strong>: the full url to the EPSG code description page</li>
           </ul>
         </li>
       </ul>
       
       <h3>Examples</h3>
       <p>Try out the following requests against prj2EPSG:</p>
       <ul>
         <li>Searching for EPSG codes in Italy:<br/>
             <a href="search.json?terms=Italy">${html_webroot}/search.json?terms=Italy</a>
             <!-- Formatted using http://jsonformat.com -->
             <pre>{ "codes" : [ { "code" : "3003",
        "name" : "Monte Mario / Italy zone 1",
        "url" : "${html_webroot}/epsg/3003.json"
      },
      { "code" : "3004",
        "name" : "Monte Mario / Italy zone 2",
        "url" : "${html_webroot}/epsg/3004.json"
      },
      { "code" : "26591",
        "name" : "Monte Mario (Rome) / Italy zone 1",
        "url" : "${html_webroot}/epsg/26591.json"
      },
      { "code" : "26592",
        "name" : "Monte Mario (Rome) / Italy zone 2",
        "url" : "${html_webroot}/epsg/26592.json"
      }
    ],
  "exact" : false,
  "totalHits" : 4
}</pre>
         </li>
         <li>Searching for EPSG codes related to "New York West" (mind the quotes):
             <br/><a href="search.json?terms=%22New York West%22">${html_webroot}/search.json?terms=%22New York West%22</a>
<pre>{ "codes" : [ { "code" : "2262",
        "name" : "NAD83 / New York West (ftUS)",
        "url" : "${html_webroot}/epsg/2262.json"
      },
      { "code" : "2830",
        "name" : "NAD83(HARN) / New York West",
        "url" : "${html_webroot}/epsg/2830.json"
      },
      { "code" : "2907",
        "name" : "NAD83(HARN) / New York West (ftUS)",
        "url" : "${html_webroot}/epsg/2907.json"
      },
      { "code" : "3629",
        "name" : "NAD83(NSRS2007) / New York West",
        "url" : "${html_webroot}/epsg/3629.json"
      },
      { "code" : "3630",
        "name" : "NAD83(NSRS2007) / New York West (ftUS)",
        "url" : "${html_webroot}/epsg/3630.json"
      },
      { "code" : "32017",
        "name" : "NAD27 / New York West",
        "url" : "${html_webroot}/epsg/32017.json"
      },
      { "code" : "32117",
        "name" : "NAD83 / New York West",
        "url" : "${html_webroot}/epsg/32117.json"
      }
    ],
  "exact" : false,
  "totalHits" : 7
}</pre>
         </li>
         <li>Searching for the Finland Uniform Coordinate System with a WKT representation:<br/>
             <a href="${html_webroot}/search.json?mode=wkt&terms=PROJCS[%22KKJ+%2F+Finland+Uniform+Coordinate+System%22%2CGEOGCS[%22KKJ%22%2CDATUM[%22D_KKJ%22%2CSPHEROID[%22International_1924%22%2C6378388%2C297]]%2CPRIMEM[%22Greenwich%22%2C0]%2CUNIT[%22Degree%22%2C0.017453292519943295]]%2CPROJECTION[%22Transverse_Mercator%22]%2CPARAMETER[%22latitude_of_origin%22%2C0]%2CPARAMETER[%22central_meridian%22%2C27]%2CPARAMETER[%22scale_factor%22%2C1]%2CPARAMETER[%22false_easting%22%2C3500000]%2CPARAMETER[%22false_northing%22%2C0]%2CUNIT[%22Meter%22%2C1]]">${html_webroot}/search.json?mode=wkt&terms=PROJCS[%22KKJ+%2F+Finland+Uniform+Coordinate+System%22%2CGE...</a>
<pre>{ "codes" : [ { "code" : "2393",
        "name" : "KKJ / Finland Uniform Coordinate System",
        "url" : "${html_webroot}/epsg/2393.json"
      } ],
  "exact" : true
}</pre>
         </li>
       </ul>       

<#include "tail.ftl">
