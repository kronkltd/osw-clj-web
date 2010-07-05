(ns osw-clj-web.profile
  (:use osw-clj-web.view
        render-ring.core)
  (:require [osw-lib-clj.core :as osw]
            [osw-clj-web.activity :as activity]
            [osw-clj-web.actor :as actor]
            [hiccup.form-helpers :as f]))

(defn form
  [request]
  (f/form-to [:post "/profile"]
             [:p (f/label :name "Name:")
              (f/text-field :name)]
             [:p (f/label :bio "Bio:")
              (f/text-field :bio)]
             [:p (f/submit-button "Submit")]))

(defn subscribers
  [jid]
  (if-let [subscribers (osw/subscribers jid)]
    [:section.subscribers
     [:h "Subscribers:"]
     [:ul
      (doall
       (map
        (fn [subscriber]
          [:li
           (actor/avatar-for subscriber)
           (actor/link-to subscriber)])
        subscribers))]]))

(defn subscriptions
  [jid]
  (if-let [subscriptions (osw/subscriptions jid)]
    [:section.subscriptions
     [:h "Subscriptions:"]
     [:ul
      (doall
       (map
        (fn [subscription]
          [:li
           (actor/avatar-for subscription)
           (actor/link-to subscription)])
        subscriptions))]]))

(defn section
  [jid]
  (let [profile (osw/profile jid)]
    (list
     [:h (if jid
         (str "Profile for " jid)
         "Profile")]
     [:section.profile
      (dump profile)
      [:p "Name: " (.getFullName profile)]
      (if-let [photo-uri (.getPhotoUri profile)]
        [:img {:src photo-uri }])
      [:p "Note: " (.getNote profile)]
      [:p "Gender: " (.getGender profile)]
      (if-let [birthday (.getBirthday profile)]
        [:p "Birthday: " birthday])]
     (subscribers jid)
     (subscriptions jid)
     (if-let [activities (osw/activities jid)]
       [:section.activities
        (doall (map #(-> % activity/line) activities))]))))

(defn show
  [request] request)

(defpage #'show :html
  [{{jid "jid"} :params,
    :as request}]
  {:body (section jid)})

(defn create
  [request]
  {:body (activity/form)})

(defn edit
  [request]
  {:body (form request)})
