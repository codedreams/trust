(ns trust.escape-test
  (:use clojure.test
        clojure.walk
        midje.sweet
        trust.escape))

(def session
  {:id "some-id"
   :name "<script>'bad&stuff</script>"
   :name-json "<script>more&bad</script>"
   :nested {:me {:you ["<" ">"] :us {:them #{">"}}}}
   :friends ["<" ">"]
   :contacts #{">" "<"}
   :address "<IMG SRC=\"javascript:alert(\"XSS\");\">"
   :role "<h1>user</h1>"})

(fact "Should return html character entities - escape-html"
      (escape-html (:name session)) =>
      "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#x2F;script&gt;")

(fact "Should not double escape when ran twice -escape-html"
      (escape-html (escape-html (:name session))) =>
      "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#x2F;script&gt;")

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
        "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#x2F;script&gt;"
        (:name-json result) =>
        "\\u003Cscript\\u003Emore\\u0026bad\\u003C/script\\u003E"
        (:role result) =>
        "<h1>user</h1>"))

(fact "Should return html escaped values by default - html-escape"
      (:name-json (html-escape session)) =>
      "&lt;script&gt;more&amp;bad&lt;&#x2F;script&gt;")

(fact "Should return json escaped values by default - json-escape"
      (:name (json-escape session)) =>
      "\\u003Cscript\\u003E'bad\\u0026stuff\\u003C/script\\u003E")


(fact "Should return HTML escaped values with same type for another function
       - xss"
      (let [result-map (xss identity session)
            result-vec (xss identity ["<h1>" "<script>"])
            result-set (xss identity #{"<h1>" "<script>"})
            result-str (xss identity "<script>")]
        
        ;;; Map
        (count result-map) => (count session)
        (map? result-map) => true
        (:name result-map) => "&lt;script&gt;&#x27;bad&amp;stuff&lt;&#x2F;script&gt;"
        (:role result-map) => "&lt;h1&gt;user&lt;&#x2F;h1&gt;"

        ;;; Vector
        (count result-vec) => 2
        (vector? result-vec) => true
        result-vec => ["&lt;h1&gt;" "&lt;script&gt;"]

        ;;; Set
        (count result-set) => 2
        (set? result-set) => true
        result-set => #{"&lt;h1&gt;" "&lt;script&gt;"}

        ;;; String
        (string? result-str) => true
        result-str => "&lt;script&gt;"))

(fact "Should return JSON escaped values with same type for another function
       - xss-json"
      (let [result-map (xss-json identity session)
            result-vec (xss-json identity ["<h1>" "<script>"])
            result-set (xss-json identity #{"<h1>" "<script>"})
            result-str (xss-json identity "<script>")]
        
        ;;; Map
        (count result-map) => (count session)
        (map? result-map) => true
        (:name result-map) =>
        "\\u003Cscript\\u003E'bad\\u0026stuff\\u003C/script\\u003E"
        (:role result-map) =>
        "\\u003Ch1\\u003Euser\\u003C/h1\\u003E"
        
        ;;; Vector
        (count result-vec) => 2
        (vector? result-vec) => true
        result-vec => ["\\u003Ch1\\u003E" "\\u003Cscript\\u003E" ]

        ;;; Set
        (count result-set) => 2
        (set? result-set) => true
        result-set => #{"\\u003Ch1\\u003E" "\\u003Cscript\\u003E"}

        ;;; String
        (string? result-str) => true
        result-str => "\\u003Cscript\\u003E"))

(facts "xss functions should escape collections in maps"
       (fact "xss should return escaped values"
             (:friends (xss identity session)) => ["&lt;" "&gt;"])
       (fact "xss should return escaped values"
             (:contacts (xss identity session)) => #{"&lt;" "&gt;"})
       (fact "xss should escape vectors"
             (vector? (:friends (xss identity session))) => true)
       (fact "xss should escape sets"
             (set? (:contacts (xss identity session))) => true)
       
       (fact "xss-json should return escaped values"
             (:friends (xss-json identity session)) => ["\\u003C" "\\u003E"])
       (fact "xss-json should return escaped values"
             (:contacts (xss-json identity session)) => #{"\\u003C" "\\u003E"})
       (fact "xss-json should escape vectors"
             (vector? (:friends (xss-json identity session))) => true)
       (fact "xss-json should escape sets"
             (set? (:contacts (xss-json identity session))) => true))

(facts "html-escape should escape collections in maps"
       (fact "should return escaped values"
             (:friends (html-escape session)) => ["&lt;" "&gt;"])
       (fact "should return escaped values"
             (:contacts (html-escape session)) => #{"&lt;" "&gt;"})
       (fact "should escape vectors"
             (vector? (:friends (html-escape session))) => true)
       (fact "should escape sets"
             (set? (:contacts (html-escape session))) => true))

(facts "json-escape should escape collections in maps"
       (fact "return escaped values"
             (:friends (json-escape session)) => ["\\u003C" "\\u003E"])
       (fact "return escaped values"
             (:contacts (json-escape session)) => #{"\\u003C" "\\u003E"})
       (fact "escape vectors"
             (vector? (:friends (json-escape session))) => true)
       (fact "escape sets"
             (set? (:contacts (json-escape session))) => true))

(facts "html-escape should escape nested map"
       (fact "should escape nested map"
             (:nested (html-escape session))
             => {:me {:us {:them #{"&gt;"}} 
                      :you ["&lt;" "&gt;"]}})
       (fact "should return correct type"
             (set? (:them (:us (:me (:nested (html-escape session))))))
             => true)
       (fact "should return escaped vector"
             (vector? (:you (:me (:nested (html-escape session)))))
             => true)
       (fact "should allow optional escape fns"
             (:nested (html-escape session {:them escape-json :you escape-json}))
             => {:me {:us {:them #{"\\u003E"}} 
                      :you ["\\u003C" "\\u003E"]}}))

(facts "xss and xss-json should escape nested collections"
       (fact "should escape nested map"
             (:nested (xss identity session))
             => {:me {:us {:them #{"&gt;"}} 
                      :you ["&lt;" "&gt;"]}})
       (fact "xss-json should return escaped nested map"
             (:nested (xss-json identity session))
             => {:me {:us {:them #{"\\u003E"}} 
                      :you ["\\u003C" "\\u003E"]}}))



