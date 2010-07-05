(ns osw-clj-web.Servlet
  (:use ring.util.servlet)
  (:require [osw-clj-web.routes :as routes]
            [osw-clj-web.service :as service]
            [swank.swank :as swank])
  (:gen-class
   :extende javax.servlet.http.HttpServlet))

;; (defn start
;;   [port]
;;   (future (swank/start-repl))
;;   (future (run-jetty (#'routes/app) {:port port})))

;; (defservice routes/app)

(defservice (fn [request] "foo"))

;; (defn -main
;;   ([]
;;      (-main 8081))
;;   ([port & args]
;;      (println "running -main")
;;      (start (or (Integer/parseInt port) 8081))))

(println "parsing core")
