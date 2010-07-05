(ns osw-clj-web.service
  (:require [osw-lib-clj.core :as osw]
            [clj-time.core :as time])
  (:import org.onesocialweb.client.exception.ConnectionRequired))

(def *session-key* nil)
(def *service-map* (ref {}))
(def *service-heartbeats* (ref {}))
(def *heartbeat-duration* (time/minutes 5))
(def *expiration-check-duration* 30000)

(defn update-heartbeat
  [session-key]
  (dosync
   ((alter *service-heartbeats*
           assoc session-key (time/now))
    session-key)))

(defn expired?
  [time]
  (time/after?
   (time/now)
   (time/plus time *heartbeat-duration*)))

(defn heartbeat
  [key]
  (@*service-heartbeats* key))

(defn assoc-service
  [service key]
  (dosync
   (alter *service-map* assoc key service)))

(defn dissoc-service
  [key]
  (dosync
   (alter *service-map* dissoc key)))

(defn new-service
  [key]
  (if-let [s (@*service-map* key)]
    s
    (let [created-service (osw/create-service)]
      (assoc-service created-service key)
      created-service)))

(defn establish-connection
  [username password hostname]
  (try
    (osw/connect! hostname)
    (osw/login! username password)))

(defn remove-connection
  [key]
  (if-let [s (@*service-map* key)]
    (do
      (try
        (osw/disconnect!)
        (catch ConnectionRequired e nil))
      (dissoc-service key))))

(defn check-expiration
  [key]
  (let [time (heartbeat key)]
    (if (expired? time)
      (remove-connection key))))

(defn check-expirations
  []
  (doseq [[key service] @*service-map*]
    (check-expiration key)))

(defn monitor-heartbeats
  []
  (Thread/sleep *expiration-check-duration*)
  (.run (Thread. check-expirations))
  (recur))

