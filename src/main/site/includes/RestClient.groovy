@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
@Grab('commons-codec:commons-codec:1.9')

import groovyx.net.http.*
import static groovyx.net.http.ContentType.JSON

[
        post: { String url, Map postParams ->
            def tmp = new URL(url)
            new RESTClient("$tmp.protocol://$tmp.authority").post(
                    path: tmp.path,
                    body: postParams,
                    requestContentType: JSON
            )
        }
]

