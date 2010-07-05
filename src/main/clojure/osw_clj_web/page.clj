(ns osw-clj-web.page
  (:use hiccup.core
        osw-clj-web.view
        render-ring.core)
  (:require [osw-clj-web.section :as section]
            [osw-lib-clj.core :as osw]
            [osw-clj-web.service :as service]
            [osw-clj-web.config :as config]
            [osw-clj-web.activity :as activity]
            [osw-clj-web.profile :as profile]
            [hiccup.form-helpers :as f]
            [osw-lib-clj.activity :as activity-model]
            [osw-lib-clj.acl :as acl-model]))

(defn main-navigation
  []
  [:nav
   [:ul
    [:li [:a {:href "/"} "Home"]]
    [:li [:a {:href "/inbox"} "Inbox"]]
    [:li [:a {:href "/profile"} "profile"]]
    [:li [:a {:href "/post"} "Post"]]
    [:li [:a {:href "/roster"} "Roster"]]]])

(defn link-to-script
  [href]
  [:script
   {:lang "javascript" :src href}])

(defn link-to-stylesheet
  [href]
  [:link
   {:type "text/css"
    :href href
    :rel "stylesheet"
    :media "screen"}])

(defn page-template
  [handler request]
  (if-let [response (handler request)]
    (merge
     response
     {:headers {"Content-Type" "text/html"}
      :body
      (str
       "<!doctype html>\n"
       (html
        [:html
         [:head
          [:title (or (:title response)
                      "OSW Clojure Web")]
          (if config/*google-maps-api-key*
            (link-to-script
             (str "http://maps.google.com/maps?file=api&amp;v=2"
                  "&amp;sensor="
                  config/*sensor*
                  "&amp;key="
                  config/*google-maps-api-key*)))
          (link-to-stylesheet "/public/css/smoothness/jquery-ui-1.8.4.custom.css")
          (link-to-script "/public/js/jquery-1.4.2.min.js")
          (link-to-script "/public/js/jquery-ui-1.8.4.custom.min.js")
          (link-to-stylesheet "/public/google-code-prettify/src/prettify.css")
          (link-to-script "/public/google-code-prettify/src/prettify.js")
          (link-to-script "/public/standard.js")
          (link-to-stylesheet "/public/standard.css")]
         [:body
          [:section.content
           [:header
            (main-navigation)
            (section/login-section request)]
           (:body response)
           [:footer
            (dump request)]]]]))})))

(defn wrap-template
  [handler]
  (partial page-template handler))

(defn comment-template
  [request]
  {:headers {"Content-Type" "text/html"}
   :body
   (html
    (activity/form request))})

(defaction login
  [{{username "username",
     password "password"} :params}]
  (merge
   {:status 302,
    :headers {"Location" "/inbox"}}
   (if (and username password)
     {:session {:username username, :password password}})))

(defaction logout
  [request]
  (service/remove-connection service/*session-key*)
  {:session {:username nil, :password nil},
   :status 302
   :headers {"Location" "/"}})

(defn root-page [request] request)
(defn inbox-page [request]
  (let [inbox (osw/inbox)]
    (.refresh inbox)
    {:activities (apply vector (.getEntries inbox))
     :request request}))

(defn host-meta
  [request]
  [:XRD
   {:xmlns "http://docs.oasis-open.org/ns/xri/xrd-1.0"
    :xmlns/hm "http://host-meta.net/ns/1.0"}
   [:hm/Host "mycyclopedia.net"]
   [:Subject "Host meta"]
   [:Link
    {:rel "lrdd"
     :template "http://mycyclopedia.net/describe?uri={uri}"}
    [:Title "Resource Descriptior"]]])

(defpage #'root-page :html
  [request]
  {:body
   [:div
    [:p "Hello World"]]})

(defpage #'inbox-page :html
  [{:keys [request activities]}]
  {:body
   (list
    (activity/form request)
    (activity/section activities))})

(defpage #'inbox-page :atom
  [{:keys [request activities]}]
  {:body
   (str
    "<feed>"
    (apply
     str
     (map
      #(activity/to-xml %)
      activities))
    "</feed>")})
