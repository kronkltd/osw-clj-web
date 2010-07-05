(ns osw-clj-web.roster
  (:use osw-clj-web.view
        render-ring.core)
  (:require [osw-lib-clj.core :as osw]
            [clojure.contrib.json :as json]))

(defn line
  [{:keys [available value label]}]
  [:li available " " value " " label])

(defn index [request]
  (let [term ((:params request) "term")
        roster (osw/roster)
        entries (.getEntries roster)
        formatted-entries
        (map
         (fn [entry]
           (let [user (.getUser entry)
                 name (.getName entry)
                 presence (.getPresence roster user)]
             {:available (.isAvailable presence)
              :label (or name user)
              :value user}))
         entries)
        filtered-entries
        (if term
          (filter
           (fn [{:keys [value label]}]
             (or
              (and value (.contains value term))
              (and label (.contains label term))))
           formatted-entries)
          formatted-entries)]
    (println term)
    filtered-entries))

(defpage #'index :html
  [entries]
  {:body
   (list
    [:h "Roster"]
    [:ul (map line entries)])})

(defpage #'index :csv
  [entries]
  {:headers {"Content-Type" "text/plain"}
   :body
   (apply str
          (map (fn [{:keys [user name available]}]
                 (str name "," user  "," available "\n"))
               entries))})

(defpage #'index :json
  [entries]
  {:headers {"Content-Type" "application/json"}
   :body
   (json/json-str entries)})
