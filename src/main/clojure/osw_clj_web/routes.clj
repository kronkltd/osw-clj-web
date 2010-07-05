(ns osw-clj-web.routes
  (:use compojure.core
        render-ring.core
        (ring.middleware session flash params)
        osw-clj-web.middleware
        hiccup.core)
  (:require [osw-clj-web.activity :as activity]
            [osw-clj-web.page :as page]
            [osw-clj-web.profile :as profile]
            [osw-clj-web.roster :as roster]
            [compojure.route :as route]))

(defroutes bare-routes
  (GET "/.well-known/host-meta" request
       {:action #'page/host-meta
        :format :xml
        :request request})
  (GET "/roster.js" request
       {:action #'roster/index
        :format :json
        :request request})
  (GET "/roster.csv" request
       {:action #'roster/index
        :format :csv
        :request request})
  (GET "/inbox.atom" request
       {:action #'page/inbox-page
        :format :atom
        :request request
        :wrappers [#'wrap-connection]}))

(defroutes page-routes
  (GET "/" request
       {:action #'page/root-page,
        :format :html,
        :request request})
  (GET "/inbox" request
       {:action #'page/inbox-page
        :format :html
        :request request
        :wrappers [#'wrap-connection]})
  (GET "/post" request
       {:action #'activity/create
        :format :html
        :request request})
  (GET "/posts/:id" request
       (println request)
       {:action #'activity/show,
        :format :html
        :request
        ((:route-params request) "id")
        #_(assoc-in request [:params "id"] ((:route-params request) "id"))})
  (GET "/profile" request
       {:action #'profile/show
        :format :html
        :request request})
  (GET "/roster" request
       {:action #'roster/index
        :format :html
        :request request})
  (GET "/profile/edit" request
       {:action #'profile/edit
        :format :html
        :request request}))

(defroutes action-routes
  (POST "/login" request
        (page/login request))
  (POST "/logout" request
        (page/logout request))
  (POST "/post" request
        (activity/create-handler request))
  (DELETE "/posts/:id" request
          (activity/delete-post-handler request)))

(defroutes template-routes
  (GET "/post.template" request
       {:body (html (activity/form))})
  (GET "/posts/:id/comment.template" request
       (page/comment-template request)))

(defroutes all-routes
  (-> (var page-routes)
      wrap-debug-binding
      ;; wrap-session
      ;; wrap-connection
      with-render
      page/wrap-template)
  (-> (var bare-routes)
      with-render)
  (var action-routes)
  (var template-routes)
  (route/files "/public")
  (route/not-found "/public/404.html"))

(def global-wrappers
     [#'wrap-user-info
      #'wrap-user-binding
      ;; #'wrap-params
      #'wrap-log-request
      ;; #'wrap-flash
      #'wrap-heartbeat-update
      #'wrap-service-binding
      #'wrap-session-key-binding
      #'wrap-session
      #'wrap-error-catching])

(defn app
  []
  (reduce
   #(%2 %1)
   (apply vector #'all-routes global-wrappers)))

