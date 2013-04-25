(ns trust.escape)

(defn escape-html
  "change special character into html character entitites"
  [text]
  (-> (str text)
      (.replace "&" "&amp;")
      (.replace "<" "&lt;")
      (.replace ">" "&gt;")
      (.replace "\"" "&quot;")
      (.replace "'" "&#x27;")
      (.replace "/" "&#2F;")))

(defn unescape-html
  "change html character entities into special characters"
  [text]
  (-> (str text)
      (.replace "&amp;" "&")
      (.replace "&lt;" "<")
      (.replace "&gt;" ">")
      (.replace "&quot;" "\"")
      (.replace "&#x27;" "'")
      (.replace "&#2F;" "/")))

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
                                :__anti-forgery-token \"some-token\"}
                               {:__anti-forgery-token identity})

   In the example above the identity function will just produce the actual value
   instead of escaping. You can have any escape-function you want to perform on
   that particular value."
  
  [escape-fn m & [opts]]
      (reduce conj {} (for [[k v] m
                            :let [v (if (k opts)
                                      ((k opts) v)
                                      (escape-fn v))]]
                        [k v])))


