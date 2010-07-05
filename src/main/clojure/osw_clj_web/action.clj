(ns
    ^{:author "Daniel E. Renfer"}
    osw-clj-web.action)

(defn line
  "Displays a action as a html line item"
  [action]
  [:li
   [:ul
    [:li "Name: " (.getName action)]
    [:li "Permission: " (.getPermission action)]]])

