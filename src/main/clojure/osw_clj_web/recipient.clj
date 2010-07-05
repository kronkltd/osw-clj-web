(ns osw-clj-web.recipient
  (:require [osw-clj-web.actor :as actor]
            [osw-lib-clj.core :as osw]))

(defn section
  [activity]
  (if-let [recipients (:recipients activity)]
    [:div.recipients
     [:ul
      (map
       (fn [recipient]
         (let [jid (.getHref recipient)]
           (if (not (re-find #"^xmpp:" jid))
             [:li.recipient
              ;; (actor/avatar-for jid)
              (actor/link-to jid)])))
       recipients)]]))

