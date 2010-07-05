(ns osw-clj-web.page-test
  (:use [osw-clj-web.page] :reload-all)
  (:use [clojure.test]))

(deftest test-root-page
  (is (map? (root-page
             {:request-method :get
              :uri "/"}))))
