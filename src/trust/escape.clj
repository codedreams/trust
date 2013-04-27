(ns trust.escape)

(defn escape-html
  "change special character into html character entitites"
  [text]
  (-> (str text)
      (clojure.string/replace
       #"&(?!(amp;|lt;|gt;|quot;|#x27;|#x2F;))" "&amp;")
      ;(.replace "&" "&amp;")
      (.replace "<" "&lt;")
      (.replace ">" "&gt;")
      (.replace "\"" "&quot;")
      (.replace "'" "&#x27;")
      (.replace "/" "&#x2F;")))

(defn unescape-html
  "change html character entities into special characters"
  [text]
  (-> (str text)
      (.replace "&amp;" "&")
      (.replace "&lt;" "<")
      (.replace "&gt;" ">")
      (.replace "&quot;" "\"")
      (.replace "&#x27;" "'")
      (.replace "&#x2F;" "/")))

(defn escape-json
  "change special character into js character entities"
  [text]
  (-> (str text)
      (.replace "&" "\\u0026")
      (.replace ">" "\\u003E")
      (.replace "<" "\\u003C")))

(defn unescape-json
  "change js characters entities into special characters"
  [text]
  (-> (str text)
      (.replace "\\u0026" "&")
      (.replace "\\u003E" ">")
      (.replace "\\u003C" "<")))


(defn escape-it 
  "Takes a default escape function, a map to escape, and gives the option to
   skip or use another escape function in a map.

   e.g. (escape-it escape-html {:name \"<script>bad-stuff</script>\"}) 
   e.g. (escape-it escape-html {:name \"<script>bad-stuff</script>\"
                                :id \"some-token\"}
                               {:id identity})

   In the example above the identity function will just produce the actual value
   instead of escaping. You can have any escape-function you want to perform on
   that particular value."
  
  [escape-fn m & [opts]]
      (reduce conj {} (for [[k v] m
                            :let [v (if (k opts)
                                      ((k opts) v)
                                      (escape-fn v))]]
                        [k v])))

(defn html-escape
  "Escape function using html as default escape.
   Takes a map and gives the option to skip or use another
   escape function"
  [m & [opts]]
  (escape-it escape-html m opts))

(defn json-escape
  "Escape function using json as default escape.
   Takes a map and gives the option to skip or use another
   escape function"
  [m & [opts]]
  (escape-it escape-json m opts))

(defn escaper
  "Used to determine value and correct operation"
  [v escape-fn]
  (cond
   (vector? v) (mapv escape-fn v)
   (set? v) (set (map escape-fn v))
   (map? v) (escape-it escape-fn v)
   :else (escape-fn v)))

(defn xss
  "Used to escape HTML arguments before applying to function"
  [fn & args]
  (apply fn (map #(escaper % escape-html) args)))

(defn xss-json
  "Used to escape JSON arguments before applying to function"
  [fn & args]
  (apply fn (map #(escaper % escape-json) args)))

