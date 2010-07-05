(ns osw-clj-web.subject)

(defn line
  [subject]
  [:li
   [:ul
    [:li "Name: " (.getName subject)]
    [:li "Type: " (.getType subject)]]])
