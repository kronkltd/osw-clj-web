(ns osw-clj-web.action-test
  (:use [osw-clj-web.action] :reload-all)
  (:use clojure.test)
  (:require [osw-lib-clj.acl :as acl]))

(deftest test-line
  (let [action (acl/action :grant :view)]
    (is (vector? (line action))
        "should be a vector")
    (is (= (first (line action)) :li)
        "should represent a list element")))
