(ns osw-clj-web.middleware-test
  (:use [osw-clj-web.middleware] :reload-all)
  (:use clojure.test))

(deftest test-wrap-heartbeat-update)

(deftest test-wrap-debug-binding)

(deftest test-wrap-session-key-binding)

(deftest test-wrap-service-binding)

(deftest test-wrap-user-binding)

(deftest test-wrap-error-catching)

(deftest test-wrap-log-request)

(deftest test-wrap-user-info)

(deftest test-wrap-vectored-params)

(deftest test-wrap-connection)
