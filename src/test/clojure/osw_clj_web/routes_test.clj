(ns osw-clj-web.routes-test
  (:use [osw-clj-web.routes] :reload-all)
  (:use clojure.test))

(defn expect-status
  [handler method uri status]
  (= status
     (:status
      (handler
       {:request-method method,
        :uri uri}))))

(deftest test-page-routes
  (is (expect-status page-routes :get "/" 200)
    "should respond to")
  (is (expect-status page-routes :get "/inbox" 200)
    "should respond to the inbox page")
  (is (expect-status page-routes :get "/post" 200)
    "should respond to the post page")
  (is (expect-status page-routes :get "/profile" 200)
    "should respond to the profile page")
  (is (expect-status page-routes :get "/roster" 200)
    "should respond to the roster page")
  (is (expect-status page-routes :get "/profile/edit" 200)
    "should respond to the profile edit page"))

#_(describe action-routes
  (it "should respond to the login action"
    (expect-status action-routes :post "/login" 302))
  (it "should respond to the logout action"
    (expect-status action-routes :post "/logout" 302)))


