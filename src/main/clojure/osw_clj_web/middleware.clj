(ns osw-clj-web.middleware
  (:use clojure.contrib.pprint
        clojure.stacktrace
        osw-clj-web.view)
  (:require [osw-clj-web.config :as config]
            [osw-clj-web.service :as s]
            [osw-lib-clj.core :as osw]))

(defn wrap-heartbeat-update
  [handler]
  (fn [request]
    (if s/*session-key*
      (s/update-heartbeat s/*session-key*))
    (handler request)))

(defn wrap-debug-binding
  "Checks if there is a debug flag passed in the request.
Turns on debugging mode for that request."
  [handler]
  (fn [request]
    (if (and
         (:query-params request)
         ((:query-params request) "debug"))
      (binding [config/*debug* true]
        (handler request))
      (handler request))))

(defn wrap-session-key-binding
  [handler]
  (fn [request]
    (let [cookies (:cookies request)
          ring-session-key (and cookies (cookies "ring-session"))]
      (if-let [session-key (and ring-session-key (:value ring-session-key))]
        (do (if config/*print-session-key*
              (println "Session Key: " session-key))
            (binding [s/*session-key* session-key]
              (handler request)))
        (handler request)))))

(defn wrap-service-binding
  [handler]
  (fn [request]
    (osw/with-service (s/new-service s/*session-key*)
      (if config/*print-service* (println osw/*service*))
      (handler request))))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (let [session (:session request)
          username (:username session)
          password (:password session)]
      (binding [osw/*user* (ref {:username username,
                                 :password password
                                 :hostname "mycyclopedia.net"})]
        (handler request)))))

(defn wrap-error-catching
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception e
       {:body (str "An error done happened: "
                   (with-out-str
                     (print-cause-trace e)))}))))

(defn wrap-log-request
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (do
        (if config/*print-request*
          (pprint request))
        response))))

(defn wrap-user-info
  [handler]
  (fn [request]
    (let [session (:session request)
          username (:username session)
          password (:password session)]
      (if config/*print-user-info*
        (println
         "User: " username
         " / "
         "Password:" password))
      (handler request))))

(defn wrap-vectored-params
  [handler]
  (fn [request]
    (handler
     (merge
      request
      {:query-params
       (into {}
             (map
              (fn [[k v]]
                [k (if (and (.endsWith k "[]")
                            (not (vector? v)))
                     [v] v)])
              (:query-params request)))}))))


(defn wrap-connection
  [handler]
  (fn [request]
    (if (and osw/*service*
             osw/*user*
             (not (osw/connected?))
             (not (osw/authenticated?)))
      (s/establish-connection (:username @osw/*user*)
                              (:password @osw/*user*)
                              (:hostname @osw/*user*)))
    (handler request)))
