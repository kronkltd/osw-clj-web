(ns osw-clj-web.section
  (:require [hiccup.form-helpers :as f]))

(defn login-section
  [{{username :username,
     password :password} :session}]
  [:div.login
   (if username
     (f/form-to
      [:post "/logout"]
      [:p
       [:span "Logged in as " username]
       (f/submit-button "logout")])
     (f/form-to
      [:post "/login"]
      [:p
       (f/label :username "Username:")
       (f/text-field :username)
       (f/label :password "Password:")
       (f/password-field :password)
       (f/submit-button "Login")]))])



