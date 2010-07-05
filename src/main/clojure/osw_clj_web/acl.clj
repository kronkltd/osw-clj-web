(ns osw-clj-web.acl
  (:require [osw-clj-web.action :as action]
            [osw-clj-web.subject :as subject]))

(defn line
  [rule]
  [:li
   [:ul
    (if-let [subjects (.getSubjects rule)]
      [:li "Subjects: "
       [:ul
        (map subject/line subjects)]])
    (if-let [actions (.getActions rule)]
      [:li "Actions: "
       [:ul
        (map action/line actions)]])]])

(defn section
  [activity]
  (if-let [rules (:aclRules activity)]
    [:li "ACL Rules: "
     [:ul
      (map line rules)]]))

