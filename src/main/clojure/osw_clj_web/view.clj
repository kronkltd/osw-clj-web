(ns osw-clj-web.view
  (:use hiccup.core
        clojure.contrib.pprint)
  (:require [osw-clj-web.config :as config]
            [osw-lib-clj.core :as osw]))

(defmacro defaction
  [name args & forms]
  `(defn ~name ~args ~@forms))

(defmacro with-connection
  [request & body]
  `(let [username# (-> ~request :session :username)
         password# (-> ~request :session :password)]
     (if (and username# password#)
       (osw/with-connection
         {:host config/*hostname*,
          :user username#,
          :password password#}
         (doall ~@body))
       (doall ~@body))))

(defn dump
  [val]
  (if config/*debug*
    [:p
     [:code
      [:pre
       (escape-html
        (with-out-str
          (pprint val)))]]]))

(defn dump-unescaped
  [val]
  (if config/*debug*
    [:p
     [:pre
      [:code.prettyprint
       (escape-html
        val)]]]))
