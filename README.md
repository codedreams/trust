# Trust

Trust is an escaping library meant for usage against xss

Install
-------

Add the following dependency to your `project.clj` file:

    [trust "1.0.0-beta"]

## Usage

The most basic usage of Trust is escaping strings - via
**escape-html** or **escape-json**  

```clojure
(def joe {:role "<h1>System-generated<h1>"
          :name "<i>Joe</i>"
          :address "<script>bad-stuff</script>"})                  
```

```clojure
(escape-html (:address joe)) 
=> "&lt;script&gt;bad-stuff&lt;&#x2F;script&gt;"
```

Trust has two primary was of escaping data before ouputting to the user.

Let's say you just pulled the joe map (shown above) from the database. You can escape the map before applying to any templates
-via **html-escape**, **json-escape**, or **escape-it**


```clojure
(html-escape joe {:role identity :name escape-json})
```

```clojure
=> {:address "&lt;script&gt;bad-stuff&lt;&#x2F;script&gt;", 
    :role "<h1>System-generated<h1>", 
    :name "\\u003Ci\\u003EJoe\\u003C/i\\u003E"}
```

The map 
```clojure 
{:role identity :name escape-json}
``` 
is optional.
It is used to provide custom functions or to skip values all together.


```clojure 
escape-it
``` 
takes a custom escape function and the same
optional arguments as ***html-escape***

```clojure 
(escape-it escape-json joe {:role escape-html})
```

```clojure
=> {:address "\\u003Cscript\\u003Ebad-stuff\\u003C/script\\u003E", 
    :role "&lt;h1&gt;System-generated&lt;h1&gt;", 
    :name "\\u003Ci\\u003EJoe\\u003C/i\\u003E"}
```

The second way of escaping data is performed closer to the template
level.  Trust currently provides two functions for use at the template level
- **xss** and **xss-json** 

Defining a user map named joe, and a html template named stats

```clojure 
(def joe {:name "Joe" :gender "Male" :address "<script>bad-stuff</script>"})
```

```clojure
(defhtml stats [m]
  [:div
   [:p [:strong (:name m)]]
   [:p [:strong (:gender m)]]
   [:p [:strong (:address m)]]])
```

Now use the ***xss*** function to escape the arguments

```clojure 
(xss stats joe) 
```

```html
=> <div>
	<p><strong>Joe</strong></p>
   	<p><strong>Male</strong></p>
   	<p><strong>&lt;script&gt;bad-stuff&lt;&#x2F;script&gt;</strong></p>
   </div>
```

After the template function **xss** and **xss-json** can be used with vectors, strings, maps, or sets.


## Why?

Some templating solutions in the clojure community require you to
manually escape output.  I think manually escaping each value can lead 
to costly mistakes.  My solution, while not automatic, provides a
less error prone way of providing a safer application.


## License

Copyright © 2013 

Distributed under the Eclipse Public License, the same as Clojure.# Trust



