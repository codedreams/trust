(ns trust.escape-test
  (:use clojure.test
        midje.sweet
        trust.escape))

(def session
  {:id "some-id"
   :name "<script>'bad&stuff</script>"
   :name-json "<script>more&bad</script>"
   :address "<IMG SRC=\"javascript:alert(\"XSS\");\">"
   :role "<h1>user</h1>"})


(fact "Should return html character entities - escape-html"
      (escape-html (:name session)) =>
      "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#2F;script&gt;")

(fact "Should return special characters - unescape-html"
      (unescape-html (escape-html (:name session))) =>
      (:name session))

(fact "Should return json character entities - escape-json"
      (escape-json (:name-json session)) =>
      "\\u003Cscript\\u003Emore\\u0026bad\\u003C/script\\u003E")

(fact "Should return special characters - unescape-json"
      (unescape-json (escape-json (:name-json session)))
      (:name-json session))

(fact "Each function should return escaped value - escape-it"
      (let [result (escape-it escape-html session {:name-json escape-json
                                                   :role identity})]
        (:address result) =>
        "&lt;IMG SRC=&quot;javascript:alert(&quot;XSS&quot;);&quot;&gt;"
        (:name result) =>
        "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#2F;script&gt;"
        (:name-json result) =>
        "\\u003Cscript\\u003Emore\\u0026bad\\u003C/script\\u003E"
        (:role result) =>
        "<h1>user</h1>"))

(fact "Should return html escaped values by default - html-escape"
      (:name-json (html-escape session)) =>
      "&lt;script&gt;more&amp;bad&lt;&#2F;script&gt;")

