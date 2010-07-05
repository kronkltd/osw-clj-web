(ns osw-clj-web.object-test
  (:use [osw-clj-web.object] :reload-all)
  (:use clojure.test)
  (:import org.onesocialweb.model.activity.DefaultActivityFactory))

(deftest test-section
  (testing "when there are no objects"
    (is (nil? (section (.entry (DefaultActivityFactory.)))))))
