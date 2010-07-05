(ns osw-clj-web.object
  (:use hiccup.core)
  (:import org.onesocialweb.model.activity.ActivityEntry
           org.onesocialweb.model.activity.ActivityObject))

(defn line
  [#^ActivityObject object]
  (let [type (.getType object)]
    [:section.object {:id (.getId object)}
     (map (fn [content]
            [:p.value
             (escape-html (.getValue content))])
          (.getContents object))
     (map (fn [link]
            [:img {:src (.getHref link)}])
          (.getLinks object))]))

(defn section
  [#^ActivityEntry entry]
  (if-let [objects (seq (.getObjects entry))]
    (map line objects)))
