(ns osw-clj-web.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [osw-clj-web.routes :as routes]
            [osw-clj-web.service :as service]
            [swank.swank :as swank]))

(defn start
  [port]
  (future (swank/start-repl))
  (future (run-jetty (#'routes/app) {:port port})))

(defn -main
  [port & args]
  (start (Integer/parseInt port)))
