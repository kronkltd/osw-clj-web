(ns osw-clj-web.actor
  (:use osw-clj-web.config)
  (:require [osw-lib-clj.core :as osw]))

(defn avatar-for
  [jid]
  (if-let [profile (osw/profile jid)]
    [:img {:src (.getPhotoUri profile)
           :height "48"
           :width "48"}]))

(defn link-to
  [jid]
  [:a {:href (str "/profile?jid=" jid)
       :content jid}
   (let [profile (osw/profile jid)
         name (if profile (.getFullName profile))]
     (if (and name (not= name ""))
       name
       jid))])

(defn section
  [actor]
  (if-let [jid (.getUri actor)]
    [:section.actor
     #_(avatar-for jid)
     (link-to jid)]))
